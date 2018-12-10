set outdir=../out/artifacts/IdsConfig_jar
java -agentlib:NativeDecrypt=encrypt.xml -jar EncryptEnc.jar -xml %outdir%\idsConfig.xml -src %outdir%/IdsConfig.jar -dst %outdir%/IdsConfigEnc.jar