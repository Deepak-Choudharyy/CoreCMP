#!/usr/bin/env python3
"""Generate numbered CoreCmp Feature Catalog PDF."""
from fpdf import FPDF

OUT = "docs/CoreCmp-Feature-Catalog.pdf"
MARGIN = 12
W1 = 22   # ID column
W2 = 48   # Name column


class CatalogPDF(FPDF):
    def footer(self):
        self.set_y(-10)
        self.set_font("Helvetica", "I", 7)
        self.set_text_color(120, 120, 120)
        self.cell(0, 8, f"CoreCmp Feature Catalog  |  Page {self.page_no()}", align="C")


def usable_width(pdf):
    return pdf.w - 2 * MARGIN


def heading(pdf, text, size=12):
    if pdf.get_y() > 255:
        pdf.add_page()
    pdf.ln(3)
    pdf.set_font("Helvetica", "B", size)
    pdf.set_text_color(13, 71, 161)
    pdf.set_x(MARGIN)
    pdf.multi_cell(usable_width(pdf), 6, text)
    pdf.ln(1)


def summary_box(pdf):
    pdf.set_fill_color(227, 242, 253)
    pdf.set_font("Helvetica", "B", 10)
    pdf.set_text_color(13, 71, 161)
    pdf.set_x(MARGIN)
    pdf.cell(usable_width(pdf), 8, "FEATURE COUNT SUMMARY", fill=True, new_x="LMARGIN", new_y="NEXT")
    pdf.set_font("Helvetica", "", 9)
    pdf.set_text_color(30, 30, 30)
    rows = [
        ("Implemented (DONE)", "131 features", "IMP-001 to IMP-131"),
        ("Planned (PENDING)", "186 features", "PLN-001 to PLN-186"),
        ("TOTAL", "317 features", ""),
        ("Progress", "41.3% complete", "131 of 317 done"),
    ]
    for label, count, range_ in rows:
        pdf.set_x(MARGIN)
        pdf.set_font("Helvetica", "B" if label == "TOTAL" else "", 9)
        pdf.cell(55, 6, label, border=1)
        pdf.cell(35, 6, count, border=1)
        pdf.cell(usable_width(pdf) - 90, 6, range_, border=1, new_x="LMARGIN", new_y="NEXT")
    pdf.ln(3)


def feat_row(pdf, fid, name, desc, header=False):
    if pdf.get_y() > 272:
        pdf.add_page()
        if not header:
            feat_row(pdf, "ID", "Feature", "Description", header=True)
    w3 = usable_width(pdf) - W1 - W2
    if header:
        pdf.set_font("Helvetica", "B", 7)
        pdf.set_fill_color(227, 242, 253)
    else:
        pdf.set_font("Helvetica", "", 7)
        pdf.set_fill_color(255, 255, 255)
    pdf.set_text_color(30, 30, 30)
    pdf.set_x(MARGIN)
    pdf.cell(W1, 5, fid[:10], border=1, fill=header)
    pdf.cell(W2, 5, name[:28], border=1, fill=header)
    pdf.cell(w3, 5, desc[:72], border=1, fill=header, new_x="LMARGIN", new_y="NEXT")


def section_features(pdf, title, features, start_num, prefix):
    heading(pdf, title, 10)
    feat_row(pdf, "ID", "Feature", "Description", header=True)
    for i, (name, desc) in enumerate(features):
        fid = f"{prefix}-{start_num + i:03d}"
        feat_row(pdf, fid, name, desc)


def build():
    pdf = CatalogPDF()
    pdf.set_margins(MARGIN, MARGIN, MARGIN)
    pdf.set_auto_page_break(True, margin=14)

    # Cover
    pdf.add_page()
    pdf.set_font("Helvetica", "B", 24)
    pdf.set_text_color(13, 71, 161)
    pdf.ln(20)
    pdf.cell(0, 12, "CoreCmp", align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.set_font("Helvetica", "", 14)
    pdf.set_text_color(50, 50, 50)
    pdf.cell(0, 9, "Complete Feature Catalog", align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.cell(0, 9, "Numbered List - Implemented + Planned", align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.ln(8)
    pdf.set_font("Helvetica", "", 9)
    pdf.set_text_color(90, 90, 90)
    pdf.cell(0, 6, "v1.0.03-alpha-10  |  com.corecmp  |  June 2026", align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.ln(10)
    summary_box(pdf)

    # PART A header
    pdf.add_page()
    heading(pdf, "PART A: IMPLEMENTED FEATURES (131 total)", 13)

    sections_impl = [
        ("A1. Core Facade (19)", 1, [
            ("CoreCmp.init()", "Platform initialization"),
            ("CoreCmp.location", "GPS location manager"),
            ("CoreCmp.permission", "Runtime permission manager"),
            ("CoreCmp.media", "Camera/gallery/doc picker"),
            ("CoreCmp.network", "Connectivity observer"),
            ("CoreCmp.storage", "Encrypted secure storage"),
            ("CoreCmp.haptics", "Haptic feedback"),
            ("CoreCmp.share", "Share text and files"),
            ("CoreCmp.display", "Fixed font/display scale"),
            ("CoreCmp.theme", "Light/Dark/AMOLED theme"),
            ("CoreCmp.appLock", "PIN + biometric lock"),
            ("CoreCmp.offlineQueue", "Offline API queue"),
            ("CoreCmp.formDrafts", "Form draft autosave"),
            ("CoreCmp.deepLinks", "Deep link handler"),
            ("CoreCmp.updates", "App update checker"),
            ("CoreCmp.geocoder", "Address geocoding"),
            ("isDebugEnabled", "API debug logging toggle"),
            ("defaultImagePlaceholder", "Default Lottie image placeholder"),
            ("defaultApiLoadingPlaceholder", "Default API loading Lottie"),
        ]),
        ("A2. Networking & API (18)", 20, [
            ("ApiClient GET", "Flow-based GET requests"),
            ("ApiClient POST", "Flow-based POST requests"),
            ("ApiClient PUT", "Flow-based PUT requests"),
            ("ApiClient DELETE", "Flow-based DELETE requests"),
            ("ApiClient PATCH", "Flow-based PATCH requests"),
            ("ApiConfig", "Multi-base URL, token, defaults"),
            ("Resource<T>", "Loading/Success/Error states"),
            ("ApiDispatcher", "Priority queue HIGH/NORMAL/LOW"),
            ("RequestOptions", "Retry-on-reconnect offline"),
            ("Multipart upload", "FilePart + PickedFile upload"),
            ("Mock API responses", "Endpoint mock JSON"),
            ("CoreCmpLogger", "Request/response/error logs"),
            ("applyDefaults()", "Auto headers and token"),
            ("mergeBody()", "Merge default request fields"),
            ("BaseViewModel", "collectApi() Flow helper"),
            ("SharedViewModel", "Cross-screen JSON storage"),
            ("Koin coreCmpModule()", "Dependency injection"),
            ("HttpClientProvider", "Shared Ktor client"),
        ]),
        ("A3. UI Components (23)", 38, [
            ("CustomScaffold", "Scaffold with back/actions"),
            ("CommonButton", "Debounced button"),
            ("OutLinedSimpleTextField", "Outlined text field"),
            ("CommonDropDown", "Searchable dropdown"),
            ("GenericBottomSheet", "Modal bottom sheet"),
            ("GenericTabs", "Tab row"),
            ("CustomCheckbox", "Checkbox control"),
            ("CustomRadioButton", "Radio button"),
            ("CustomImage Coil", "Image loading"),
            ("CustomImage SVG", "SVG support"),
            ("CustomImage Lottie", "Lottie in image slot"),
            ("CustomLoading", "Lottie loading"),
            ("SnackBarBoxApp", "Top snackbar container"),
            ("AppSnackbarManager", "Global snackbar queue"),
            ("GenericDatePicker", "Calendar date picker"),
            ("EasyDatePicker", "Simplified date picker"),
            ("DashedDivider", "Dashed divider"),
            ("dashedBorder", "Dashed border modifier"),
            ("beamBorder", "Gradient beam border"),
            ("bounceClick", "Spring press animation"),
            ("shimmer", "Skeleton shimmer"),
            ("CommonWebView", "Platform WebView"),
            ("Color tokens", "Shared colors and gradients"),
        ]),
        ("A4. Theme & Display (9)", 61, [
            ("CoreCmpTheme Light", "Material3 light scheme"),
            ("CoreCmpTheme Dark", "Material3 dark scheme"),
            ("CoreCmpTheme AMOLED", "Pure black theme"),
            ("ThemeManager", "Persisted theme mode"),
            ("ThemeSettingsPanel", "Theme picker UI"),
            ("CoreCmpDisplayHost", "Lock font and density"),
            ("DisplaySettingsPanel", "Fixed layout UI"),
            ("DisplaySettingsManager", "Persist display prefs"),
            ("withDisplaySettings()", "Android context fix"),
        ]),
        ("A5. Media Picker (9)", 70, [
            ("Camera picker", "Take photo"),
            ("Gallery picker", "No storage permission"),
            ("Document picker", "No storage permission"),
            ("AttachmentBottomSheet", "Camera/Photos/Docs UI"),
            ("PickedFile model", "Bytes, name, mime wrapper"),
            ("PickedFile size check", "2MB/4MB/10MB helpers"),
            ("toBase64Image()", "Base64 for API upload"),
            ("Android PickVisualMedia", "Modern Android picker"),
            ("iOS PHPicker", "Modern iOS picker"),
        ]),
        ("A6. Permissions (8)", 79, [
            ("CAMERA permission", "Runtime camera"),
            ("LOCATION permission", "Runtime GPS"),
            ("NOTIFICATION permission", "Android 13+ push"),
            ("CONTACTS permission", "Contact read"),
            ("MICROPHONE permission", "Audio recording"),
            ("GALLERY picker-only", "No READ_MEDIA_IMAGES"),
            ("DOCUMENT picker-only", "No READ_EXTERNAL_STORAGE"),
            ("Auto-grant picker perms", "Skip GALLERY/STORAGE prompt"),
        ]),
        ("A7. Security (5)", 87, [
            ("AppLockManager PIN", "Enable/verify PIN"),
            ("AppLockGate", "Lock screen overlay"),
            ("BiometricAuth Android", "Fingerprint/face"),
            ("BiometricAuth iOS", "Face ID/Touch ID"),
            ("SecureStorage encrypted", "Android+iOS encryption"),
        ]),
        ("A8. Form Drafts (2)", 92, [
            ("FormDraftManager", "Save/load/clear drafts"),
            ("rememberFormDraft()", "Composable auto-save"),
        ]),
        ("A9. Location (3)", 94, [
            ("LocationManager", "Current GPS coordinates"),
            ("Geocoder", "Address search/reverse"),
            ("LocationPickerBottomSheet", "Map pin picker UI"),
        ]),
        ("A10. Share & PDF (6)", 97, [
            ("shareText()", "Native text share"),
            ("shareFile()", "Native file share"),
            ("generateAndShare()", "Compose to PDF share"),
            ("generateAndDownload()", "Compose to PDF save"),
            ("shareScreenAsPdf()", "Quick PDF share"),
            ("shareReceipt()", "Formatted receipt share"),
        ]),
        ("A11. Network Utils (5)", 103, [
            ("isOnline", "Instant connectivity check"),
            ("connectivityFlow", "Reactive network Flow"),
            ("offlineQueue.enqueue()", "Queue failed requests"),
            ("offlineQueue.startAutoFlush()", "Auto retry online"),
            ("offlineQueue.pending()", "List queued requests"),
        ]),
        ("A12. Deep Links & Updates (6)", 108, [
            ("deepLinks.route()", "Register URL patterns"),
            ("deepLinks.handle()", "Parse and match URI"),
            ("DeepLinkMatch", "Path + query params"),
            ("updates.checkRemote()", "Remote version JSON"),
            ("openAppUpdate()", "Open store URL"),
            ("triggerNativeInAppUpdate()", "Play in-app update"),
        ]),
        ("A13. Date Extensions (6)", 114, [
            ("toDdMmmYyyy()", "dd MMM yyyy format"),
            ("toYyyyMmDd()", "yyyy/MM/dd format"),
            ("toServerDate()", "yyyy-MM-dd format"),
            ("toDateTime()", "Date with time"),
            ("toDateTimeSeconds()", "Date with seconds"),
            ("DateUtils", "currentDate, year, month"),
        ]),
        ("A14. Indian Locale (6)", 120, [
            ("toIndianCurrency()", "Rs lakh grouping"),
            ("toIndianNumber()", "Indian number format"),
            ("toIndianPhone()", "+91 phone format"),
            ("toIndianDate()", "dd/MM/yyyy"),
            ("toIndianDateTime()", "dd/MM/yyyy hh:mm a"),
            ("toCompactIndianAmount()", "1.2L / 3.5Cr format"),
        ]),
        ("A15. Utilities (6)", 126, [
            ("toTitleCase()", "String title case"),
            ("rememberDebouncedClick()", "Debounced click"),
            ("RateLimiter", "Action rate limiter"),
            ("debounce()", "Debounce helper"),
            ("CoreCmpAccessibility", "Default a11y labels"),
            ("coreCmpContentDescription()", "A11y modifier"),
        ]),
    ]

    for title, start, features in sections_impl:
        section_features(pdf, title, features, start, "IMP")

    # PART B
    pdf.add_page()
    heading(pdf, "PART B: PLANNED FEATURES (186 total)", 13)
    summary_box(pdf)

    sections_planned = [
        ("B1. File Upload Compress+Fast (20)", 1, [
            ("Image compressor", "JPEG/WebP quality reduce"),
            ("Smart size target", "Auto 2/4/10 MB"),
            ("Resolution scaler", "Max 1920px"),
            ("PDF compressor", "Reduce PDF image quality"),
            ("Pre-upload validator", "Block oversize/wrong MIME"),
            ("Compression preview", "Show before/after size"),
            ("Image cropper", "Rect/square/circle crop"),
            ("Crop-compress pipeline", "Crop then compress"),
            ("HEIC to JPEG", "iOS photo convert"),
            ("Chunked upload", "Split large files"),
            ("Parallel chunk upload", "2-3x faster"),
            ("Upload progress %", "Real-time progress"),
            ("Upload speed KB/s", "Speed indicator"),
            ("Per-chunk retry", "Retry failed chunk only"),
            ("Resumable upload", "Resume on disconnect"),
            ("Upload queue", "Multiple file queue"),
            ("Background upload", "Upload when backgrounded"),
            ("WiFi-only upload", "Skip on mobile data"),
            ("Pre-signed URL upload", "Direct S3/GCS"),
            ("CoreCmp.upload facade", "pick->compress->upload"),
        ]),
        ("B2. Form Validation & Masks (15)", 21, [
            ("PAN validator", "ABCDE1234F format"),
            ("Aadhaar validator", "Verhoeff checksum"),
            ("GSTIN validator", "15 char GST"),
            ("IFSC validator", "Bank code format"),
            ("Vehicle number validator", "MH12AB1234"),
            ("Email validator", "RFC email check"),
            ("Phone validator", "Indian 10-digit"),
            ("Pincode validator", "6-digit + city lookup"),
            ("Phone input mask", "+91 auto format"),
            ("PAN input mask", "Auto uppercase"),
            ("Currency input mask", "Rs comma grouping"),
            ("Vehicle number mask", "MH-12-AB-1234"),
            ("FormState manager", "Multi-field validation"),
            ("OTP 6-box input", "OTP entry UI"),
            ("Password strength", "Weak/medium/strong"),
        ]),
        ("B3. Networking Advanced (15)", 36, [
            ("Token auto-refresh", "401 single refresh"),
            ("Request cancellation", "Cancel on screen leave"),
            ("Exponential backoff", "Smart retry 5xx"),
            ("Request deduplication", "Block duplicate calls"),
            ("GET response cache", "TTL offline cache"),
            ("SSE helper", "Server-sent events"),
            ("WebSocket helper", "Real-time connection"),
            ("Certificate pinning", "SSL pin config"),
            ("API health ping", "Server status check"),
            ("Per-endpoint timeout", "Custom timeouts"),
            ("Request signing", "HMAC signature"),
            ("Offset pagination", "page/limit paging"),
            ("Cursor pagination", "next_cursor paging"),
            ("Download manager", "File download resume"),
            ("Multipart progress", "% on standard upload"),
        ]),
        ("B4. Navigation (12)", 51, [
            ("Type-safe routes", "Sealed class routes"),
            ("navigateOnce()", "Debounced navigate"),
            ("popUpTo helpers", "Back-stack utilities"),
            ("Back stack inspector", "Debug print stack"),
            ("Android Intent intake", "Cold start deep link"),
            ("iOS Universal Link", "NSUserActivity intake"),
            ("Push notification router", "Payload to screen"),
            ("Tab state restore", "Bottom bar state save"),
            ("Nested graph VM scope", "VM per graph"),
            ("Navigation result API", "Return data from screen"),
            ("Conditional nav guard", "Skip if condition met"),
            ("Nav animation presets", "Slide/fade animations"),
        ]),
        ("B5. UI Components New (25)", 63, [
            ("Status chip", "Pending/Approved badge"),
            ("Timeline", "Step progress UI"),
            ("Expandable card", "Accordion"),
            ("Comparison table", "Insurer quotes"),
            ("Key-value row", "Policy detail row"),
            ("Skeleton card", "Card shimmer"),
            ("Skeleton list", "List shimmer"),
            ("Skeleton form", "Form shimmer"),
            ("Empty state view", "No data UI"),
            ("Error retry view", "Error + retry button"),
            ("Pull-to-refresh", "Swipe refresh"),
            ("Swipe-to-delete", "List swipe action"),
            ("Signature pad", "Touch signature"),
            ("Rating stars", "1-5 star input"),
            ("Countdown timer", "OTP/offer timer"),
            ("Search bar", "Debounced search"),
            ("Segmented control", "Segment picker"),
            ("Info banner", "Blue info message"),
            ("Warning banner", "Yellow warning"),
            ("Error banner", "Red error message"),
            ("Amount input", "Rs auto-format"),
            ("Copyable text row", "Tap to copy"),
            ("Progress stepper", "Step 1/4 indicator"),
            ("Tooltip popover", "Info tooltip"),
            ("Badge dot", "Unread count badge"),
        ]),
        ("B6. Theme & Design (10)", 88, [
            ("CoreCmpColors injection", "Host brand colors"),
            ("CoreCmpTypography", "Custom fonts"),
            ("White-label JSON theme", "Per-client theme"),
            ("High contrast mode", "Accessibility theme"),
            ("Reduce motion", "Disable animations"),
            ("Dynamic color Android", "Material You"),
            ("Font size slider", "User text size"),
            ("Spacing tokens", "4/8/12/16/24dp"),
            ("CoreCmpProviders", "All providers bundle"),
            ("Component theme override", "Per-component colors"),
        ]),
        ("B7. Security & Privacy (12)", 98, [
            ("Session timeout", "Auto lock idle"),
            ("Screenshot block Android", "FLAG_SECURE"),
            ("Screenshot block iOS", "Secure overlay"),
            ("Screen recording block", "Detect recording"),
            ("Root detection warning", "Jailbreak warn"),
            ("PII mask PAN", "ABCDE****F display"),
            ("PII mask phone", "98****3210 display"),
            ("PII log redaction", "Strip from API logs"),
            ("Consent manager", "GDPR/DPDP checkbox"),
            ("Data export", "User data JSON export"),
            ("Auto-logout background", "Lock on background"),
            ("Encrypted drafts", "Extra draft encryption"),
        ]),
        ("B8. Indian/Insurance (15)", 110, [
            ("Rupee in words", "Number to words"),
            ("UPI deep link builder", "upi://pay builder"),
            ("Pincode to city", "Address lookup"),
            ("Age from DOB", "Premium age calc"),
            ("NCB calculator", "No-claim bonus"),
            ("IDV formatter", "Insured value display"),
            ("Premium breakdown UI", "OD+TP+GST display"),
            ("Policy expiry countdown", "Renews in X days"),
            ("KYC doc checklist", "Pending docs UI"),
            ("Insurer comparison card", "3 quotes card"),
            ("Commission calculator", "Agent payout"),
            ("EMI calculator", "Installment calc"),
            ("GST calculator", "18% tax calc"),
            ("Policy number formatter", "Display mask"),
            ("Nominee relationship list", "Dropdown data"),
        ]),
        ("B9. QR & Platform Utils (12)", 125, [
            ("QR scanner", "Camera QR scan"),
            ("QR generator", "Generate QR image"),
            ("Barcode scanner", "Policy barcode"),
            ("Clipboard copy", "Copy to clipboard"),
            ("Clipboard paste", "Read clipboard"),
            ("In-app review", "Store review prompt"),
            ("In-app browser", "WebView modal"),
            ("Open maps", "Navigate in maps"),
            ("Open dialer", "tel: intent"),
            ("Open WhatsApp", "wa.me opener"),
            ("Open email", "mailto: intent"),
            ("Device info", "Model/OS/version"),
        ]),
        ("B10. Auth & Social (8)", 137, [
            ("Google Sign-In", "Credential Manager"),
            ("Sign in with Apple", "iOS + web fallback"),
            ("OTP auto-read", "SMS Retriever Android"),
            ("Biometric re-auth", "Confirm sensitive action"),
            ("Role-based UI guard", "Agent vs customer"),
            ("Multi-account switch", "POS account switch"),
            ("Guest mode", "Browse without login"),
            ("Magic link handler", "Email verify deep link"),
        ]),
        ("B11. State Management (8)", 145, [
            ("UiState<T>", "Loading/Success/Error/Empty"),
            ("Event<T>", "One-shot rotation-safe"),
            ("SavedStateHandle KMP", "Process death restore"),
            ("MVI reduce helper", "State reducer"),
            ("Side effect channel", "SharedFlow wrapper"),
            ("Polling helper", "Check every N seconds"),
            ("Poll until condition", "Wait for success"),
            ("Flow throttle/debounce", "Flow operators"),
        ]),
        ("B12. Storage & Cache (8)", 153, [
            ("SQLDelight cache", "Local policy DB"),
            ("API GET cache TTL", "Cached responses"),
            ("Image disk cache", "Clear and size limit"),
            ("User preferences store", "Non-sensitive settings"),
            ("Schema migration", "DB version upgrade"),
            ("Storage size reporter", "App storage usage"),
            ("One-tap cache clear", "Clear all caches"),
            ("JVM storage encrypt", "Desktop encryption"),
        ]),
        ("B13. Analytics (8)", 161, [
            ("CoreCmpAnalytics interface", "Firebase/Mixpanel"),
            ("CoreCmpCrash interface", "Sentry/Crashlytics"),
            ("Screen view tracker", "Auto on navigation"),
            ("Event builder", "track(name, params)"),
            ("User properties", "Attach to all events"),
            ("Performance trace", "API duration track"),
            ("Breadcrumb log", "Pre-crash action trail"),
            ("A/B variant tag", "Experiment ID"),
        ]),
        ("B14. Notifications (7)", 169, [
            ("FCM token manager", "Firebase register"),
            ("APNs token manager", "iOS push register"),
            ("Push to deep link", "Tap to screen"),
            ("Local notification scheduler", "Renewal reminders"),
            ("In-app notification inbox", "Read/unread list"),
            ("What's new screen", "Changelog on update"),
            ("Announcement banner", "Remote config message"),
        ]),
        ("B15. Accessibility & i18n (7)", 176, [
            ("48dp touch target", "Minimum tap size"),
            ("TalkBack announce", "Android screen reader"),
            ("VoiceOver announce", "iOS screen reader"),
            ("RTL layout", "Right-to-left support"),
            ("Multi-language strings", "CMP resources"),
            ("Plural rules", "1 policy vs 5 policies"),
            ("Locale auto format", "Auto date/number"),
        ]),
        ("B16. Testing & Dev Tools (4)", 183, [
            ("Fake ApiClient", "Unit test no network"),
            ("Fake SecureStorage", "In-memory test storage"),
            ("Debug overlay shake", "Shake for API log"),
            ("Sample app template", "Integration starter"),
        ]),
    ]

    for title, start, features in sections_planned:
        section_features(pdf, title, features, start, "PLN")

    # Final summary page
    pdf.add_page()
    heading(pdf, "FINAL COUNT", 13)
    summary_box(pdf)
    pdf.set_font("Helvetica", "", 9)
    pdf.set_text_color(30, 30, 30)
    pdf.set_x(MARGIN)
    lines = [
        "Implemented: IMP-001 to IMP-131  (131 features)",
        "Planned:     PLN-001 to PLN-186  (186 features)",
        "Total:       317 features",
        "Progress:    41.3% complete",
        "",
        "Full markdown: docs/CoreCmp-Feature-Catalog.md",
        "Regenerate PDF: .pdf-venv/bin/python3 docs/generate_catalog_pdf.py",
    ]
    for line in lines:
        pdf.cell(0, 6, line, new_x="LMARGIN", new_y="NEXT")

    pdf.output(OUT)
    print(f"Generated: {OUT}")


if __name__ == "__main__":
    build()
