@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA")

package com.corecmp.shared.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionCompletionHandler
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dictionaryWithContentsOfFile
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class GoogleAuth actual constructor() {

    companion object {
        private var activeSession: ASWebAuthenticationSession? = null
        private var activeProvider: NSObject? = null
    }

    actual val isAvailable: Boolean = true

    actual suspend fun signIn(webClientId: String?): SocialAuthResult? {
        return suspendCancellableCoroutine { continuation ->
            val plistPath = NSBundle.mainBundle.pathForResource("GoogleService-Info", "plist")
            var clientId: String? = webClientId
            var reversedClientId: String? = null

            if (plistPath != null) {
                val dict = NSDictionary.dictionaryWithContentsOfFile(plistPath)
                if (clientId.isNullOrBlank()) {
                    clientId = dict?.get("CLIENT_ID") as? String
                }
                reversedClientId = dict?.get("REVERSED_CLIENT_ID") as? String

                if (clientId.isNullOrBlank()) {
                    val googleAppId = dict?.get("GOOGLE_APP_ID") as? String
                    if (!googleAppId.isNullOrBlank() && googleAppId.contains(":")) {
                        val parts = googleAppId.split(":")
                        if (parts.size >= 4) {
                            val projNum = parts[1]
                            val hash = parts[3]
                            clientId = "$projNum-$hash.apps.googleusercontent.com"
                            if (reversedClientId.isNullOrBlank()) {
                                reversedClientId = "com.googleusercontent.apps.$projNum-$hash"
                            }
                        }
                    }
                }
            }

            val effectiveClientId = clientId?.takeIf { it.isNotBlank() } ?: "822262222647-9a8ees1rbahn437ttkce5r7sbhi5d2bm.apps.googleusercontent.com"
            val redirectScheme = reversedClientId?.takeIf { it.isNotBlank() }
                ?: if (effectiveClientId.contains("-")) "com.googleusercontent.apps.${effectiveClientId.substringBefore(".apps")}"
                else "com.googleusercontent.apps.822262222647-9a8ees1rbahn437ttkce5r7sbhi5d2bm"
            val redirectUri = "$redirectScheme:/oauth2redirect"

            val nonce = NSUUID.UUID().UUIDString
            val authUrlStr = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=$effectiveClientId&" +
                    "redirect_uri=$redirectUri&" +
                    "response_type=code&" +
                    "scope=email%20profile%20openid&" +
                    "prompt=select_account&" +
                    "nonce=$nonce"

            val authUrl = NSURL.URLWithString(authUrlStr) ?: return@suspendCancellableCoroutine continuation.resume(null)

            val completionHandler: ASWebAuthenticationSessionCompletionHandler = { callbackUrl, error ->
                activeSession = null
                activeProvider = null

                if (error != null || callbackUrl == null) {
                    if (continuation.isActive) continuation.resume(null)
                } else {
                    val urlStr = callbackUrl.absoluteString ?: ""
                    val code = extractQueryOrFragmentParam(urlStr, "code")
                    val directIdToken = extractQueryOrFragmentParam(urlStr, "id_token")
                        ?: extractQueryOrFragmentParam(urlStr, "access_token")

                    if (!code.isNullOrBlank()) {
                        exchangeCodeForToken(code, effectiveClientId, redirectUri) { idToken, email, name ->
                            val finalEmail = email?.takeIf { it.isNotBlank() }
                                ?: extractQueryOrFragmentParam(urlStr, "email")
                                ?: ""
                            val finalToken = idToken?.takeIf { it.isNotBlank() }
                                ?: "ios_google_code_$code"

                            if (finalEmail.isNotBlank()) {
                                val result = SocialAuthResult(
                                    provider = SocialAuthProviderType.GOOGLE,
                                    idToken = finalToken,
                                    accessToken = finalToken,
                                    email = finalEmail,
                                    displayName = name ?: finalEmail.substringBefore("@")
                                )
                                if (continuation.isActive) continuation.resume(result)
                            } else {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }
                    } else if (!directIdToken.isNullOrBlank()) {
                        val decodedJson = decodeJwtPayload(directIdToken)
                        val email = extractQueryOrFragmentParam(urlStr, "email")
                            ?: extractParamFromJson(decodedJson, "email")
                            ?: ""
                        val displayName = extractParamFromJson(decodedJson, "name")
                            ?: extractParamFromJson(decodedJson, "given_name")
                            ?: if (email.contains("@")) email.substringBefore("@") else "User"

                        if (email.isNotBlank()) {
                            val result = SocialAuthResult(
                                provider = SocialAuthProviderType.GOOGLE,
                                idToken = directIdToken,
                                accessToken = directIdToken,
                                email = email,
                                displayName = displayName
                            )
                            if (continuation.isActive) continuation.resume(result)
                        } else {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    } else {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            }

            val session = ASWebAuthenticationSession(
                uRL = authUrl,
                callbackURLScheme = redirectScheme,
                completionHandler = completionHandler
            )

            val provider = object : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
                override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): UIWindow {
                    val keyWindow = UIApplication.sharedApplication.windows
                        .filterIsInstance<UIWindow>()
                        .firstOrNull { it.isKeyWindow() }
                        ?: UIApplication.sharedApplication.keyWindow
                        ?: UIWindow()
                    return keyWindow
                }
            }

            session.presentationContextProvider = provider
            session.prefersEphemeralWebBrowserSession = false

            activeSession = session
            activeProvider = provider

            val started = session.start()
            if (!started && continuation.isActive) {
                activeSession = null
                activeProvider = null
                continuation.resume(null)
            }
        }
    }

    actual suspend fun signOut() = Unit

    private fun extractQueryOrFragmentParam(url: String, paramName: String): String? {
        val pattern = "(?:[?&#])$paramName=([^&]+)".toRegex()
        val match = pattern.find(url)
        return match?.groupValues?.get(1)
    }

    private fun decodeJwtPayload(idToken: String?): String? {
        if (idToken == null || !idToken.contains(".")) return null
        return try {
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                var base64 = parts[1].replace('-', '+').replace('_', '/')
                val remainder = base64.length % 4
                if (remainder > 0) {
                    base64 += "=".repeat(4 - remainder)
                }
                val nsData = NSData.create(base64EncodedString = base64, options = 0UL)
                if (nsData != null) {
                    NSString.create(data = nsData, encoding = NSUTF8StringEncoding)?.toString()
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun extractParamFromJson(json: String?, paramName: String): String? {
        if (json == null) return null
        val regex = "\"$paramName\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun exchangeCodeForToken(
        code: String,
        clientId: String,
        redirectUri: String,
        completion: (idToken: String?, email: String?, name: String?) -> Unit
    ) {
        val tokenUrl = NSURL.URLWithString("https://oauth2.googleapis.com/token") ?: run {
            completion(null, null, null)
            return
        }
        val request = NSMutableURLRequest.requestWithURL(tokenUrl)
        request.setHTTPMethod("POST")
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField = "Content-Type")

        val postDataStr = "code=$code&" +
                "client_id=$clientId&" +
                "grant_type=authorization_code&" +
                "redirect_uri=$redirectUri"

        val postData = (postDataStr as NSString).dataUsingEncoding(NSUTF8StringEncoding)
        request.setHTTPBody(postData)

        val session = NSURLSession.sharedSession
        val task = session.dataTaskWithRequest(request) { data, _, error ->
            if (error != null || data == null) {
                completion(null, null, null)
                return@dataTaskWithRequest
            }

            val jsonStr = NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
            if (jsonStr != null) {
                val idToken = extractParamFromJson(jsonStr, "id_token")
                    ?: extractParamFromJson(jsonStr, "access_token")
                val decodedJson = decodeJwtPayload(idToken)
                val email = extractParamFromJson(decodedJson, "email")
                    ?: extractParamFromJson(jsonStr, "email")
                val name = extractParamFromJson(decodedJson, "name")
                    ?: extractParamFromJson(decodedJson, "given_name")

                completion(idToken, email, name)
            } else {
                completion(null, null, null)
            }
        }
        task.resume()
    }
}
