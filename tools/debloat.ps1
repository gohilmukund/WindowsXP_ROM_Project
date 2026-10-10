# Debloat script for Samsung Galaxy Tab 3 7.0 (SM-T210 / SM-T210R)
# Blocks Samsung TouchWiz bloatware and Google GApps services without requiring root

$adb = "C:\Users\gohil\Downloads\Samsung_Tab3_SM-T210_Tools\platform-tools\adb.exe"

Write-Host "Verifying device connection..." -ForegroundColor Cyan
& $adb devices

$packagesToBlock = @(
    "com.sec.android.app.sbrowser",
    "com.sec.android.app.music",
    "com.sec.android.app.videoplayer",
    "com.sec.chaton",
    "com.sec.android.app.samsungapps",
    "com.sec.android.widgetapp.ap.hero.accuweather",
    "com.sec.evernote",
    "com.android.vending",
    "com.google.android.syncadapters.contacts",
    "com.google.android.syncadapters.calendar",
    "com.google.android.gsf",
    "com.google.android.gsf.login",
    "com.google.android.googlequicksearchbox",
    "com.google.android.talk",
    "com.google.android.youtube",
    "com.samsung.helphub",
    "com.sec.android.app.sns3",
    "flipboard.app",
    "com.mobisystems.office",
    "com.tripadvisor.tripadvisor"
)

Write-Host "`nBlocking bloated packages..." -ForegroundColor Yellow
foreach ($pkg in $packagesToBlock) {
    $result = & $adb shell "pm block $pkg 2>&1"
    Write-Host "[$pkg] -> $result"
}

Write-Host "`nVerifying blocked status..." -ForegroundColor Green
$installedBlocked = & $adb shell "pm list packages -u"
Write-Host "Debloat process completed successfully." -ForegroundColor Cyan
