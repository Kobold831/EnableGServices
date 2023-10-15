# EnableGServices

[EnableGPlayWithPC](https://github.com/Kobold831/EnableGPlayWithPC)

チャレンジパッド2可能

チャレンジパッド3・NEO不可

## GServReceiverへブロードキャスト

```
adb shell am broadcast -a com.saradabar.intent.action.RUN_ENABLE_GSERV
```

## MainActivity起動

```
adb shell am start -n com.saradabar.enablegservices/.MainActivity
```

## 結果

内部ストレージcom.saradabar.enablegservices.txtを参照


CODE 0 : Success

CODE 1 : Failed to bind to DchaService

CODE 2 : Exception Occurred