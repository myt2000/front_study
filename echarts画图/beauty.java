class FUBeautyProxyImpl(
    private val sign: ByteArray,
    private val uid: String,
    private val aiFaceBundleFile: String,
    private val beautyBundleFile: String,
    private val logConfig: FUBeautyLogConfig? = null
) : IXoLBeautyProxy {

    companion object {
        internal const val TAG = "XoLFUBeauty"
        internal const val KEY_BEAUTY_SETTINGS = "KEY_BEAUTY_SETTINGS"
        private var INPUT_TEXTURE_TYPE = FUInputTextureEnum.FU_ADM_FLAG_COMMON_TEXTURE //纹理类型
        private var EXTERNAL_INPUT_TYPE = FUExternalInputEnum.EXTERNAL_INPUT_TYPE_VIDEO //数据源类型
        private var INPUT_TEXTURE_MATRIX = FUTransformMatrixEnum.CCROT0_FLIPVERTICAL //纹理旋转类型
    }

    private var mCurFaceIndex = 0 // 当前选中的美颜配置档位序号
    private var sharedPreferences: SharedPreferences? = null
    private val mFURenderKit: FURenderKit by lazy { FURenderKit.getInstance() }

    private val enginePrepared: AtomicBoolean by lazy {
        AtomicBoolean(false)
    }

    override fun prepareEngine(context: Context) {
        synchronized(this) {
            sharedPreferences = context.getSharedPreferences(
                context.packageName + "_beauty", Context.MODE_PRIVATE
            )
            if (logConfig == null) {
                setFUBeautyLogLevel(FULogger.LogLevel.OFF.ordinal)
            } else {
                setFUBeautyLogLevel(logConfig.logLevel.ordinal)
                if (logConfig.savedLogFile != null && logConfig.maxLogFileCount != null && logConfig.perLogFileSizeInBytes != null) {
                    openFileLog(path = logConfig.savedLogFile, maxFileSize = logConfig.perLogFileSizeInBytes.toInt(), maxFiles = logConfig.maxLogFileCount)
                }
            }
            registerFURender(context, sign, object : OperateCallback {
                override fun onSuccess(code: Int, msg: String) {
                    IKLog.d(TAG, "registerFURender OperateCallback onSuccess() $code $msg")
                    mFURenderKit.FUAIController.loadAIProcessor(
                        aiFaceBundleFile,
                        FUAITypeEnum.FUAITYPE_FACEPROCESSOR
                    )
                    applyLocalConfig()
                    enginePrepared.set(true)
                }

                override fun onFail(errCode: Int, errMsg: String) {
                    IKLog.e(TAG, "registerFURender OperateCallback onFail() $errCode $errMsg")
                    enginePrepared.set(false)
                }
            })
        }
    }

    override fun renderWithInput(textureId: Int, width: Int, height: Int): Int {
        return synchronized(this) {
            if (enginePrepared.get() && mFURenderKit.FUAIController.isAIProcessorLoaded(FUAITypeEnum.FUAITYPE_FACEPROCESSOR)) {
                val inputData = FURenderInputData(width, height)
                inputData.texture = FURenderInputData.FUTexture(INPUT_TEXTURE_TYPE, textureId)
                val config: FURenderInputData.FURenderConfig = inputData.renderConfig
                config.externalInputType = EXTERNAL_INPUT_TYPE
                config.inputBufferMatrix = INPUT_TEXTURE_MATRIX
                config.inputTextureMatrix = INPUT_TEXTURE_MATRIX
                try {
                    val outputData = mFURenderKit.renderWithInput(inputData)
                    if (outputData.texture != null && outputData.texture!!.texId > 0) {
                        outputData.texture!!.texId
                    } else {
                        textureId
                    }
                } catch (e:Exception) {
                    IKLog.e(TAG, "render textureId fail.${Log.getStackTraceString(e)}")
                    textureId
                }
            } else {
                textureId
            }
        }
    }

    override fun applyBeautyConfig(type: BeautyConfigType, value: Number) {
        if (enginePrepared.get()) {
            when (type) {
                BeautyConfigType.FilterIndex -> {
                    val lastFilterIndex = matchFilterIndex(
                        mFURenderKit.faceBeauty?.filterName ?: FaceBeautyFilterEnum.ORIGIN
                    )
                    val curFilterIndex = value.toInt()
                    if (lastFilterIndex >= 0 && lastFilterIndex != curFilterIndex) {
                        mFURenderKit.faceBeauty?.filterName = ConstFilterArray[curFilterIndex].key
                        mFURenderKit.faceBeauty?.filterIntensity = DEFAULT_FILTER_VALUE
                    }
                }
                BeautyConfigType.FilterValue -> {
                    val lastFilterIndex = matchFilterIndex(
                        mFURenderKit.faceBeauty?.filterName ?: FaceBeautyFilterEnum.ORIGIN
                    )
                    if (lastFilterIndex >= 0) {
                        mFURenderKit.faceBeauty?.filterIntensity = value.toDouble()
                    }
                }
                BeautyConfigType.FaceIndex -> {
                    mCurFaceIndex = value.toInt()
                }
                BeautyConfigType.Color -> {
                    mFURenderKit.faceBeauty?.colorIntensity = value.toDouble() * FACE_COLOR_MULTIPLE
                }
                BeautyConfigType.Blur -> {
                    mFURenderKit.faceBeauty?.blurIntensity = value.toDouble() * FACE_BLUR_MULTIPLE
                }
                BeautyConfigType.Eye -> {
                    mFURenderKit.faceBeauty?.eyeEnlargingIntensityV2 = value.toDouble()
                }
                BeautyConfigType.Thin -> {
                    mFURenderKit.faceBeauty?.cheekThinningIntensity = value.toDouble()
                }
            }
        }
    }

    override fun getBeautyConfig(): BeautyConfig {
        val settings = obtainBeautySettings() ?: readLocalBeautySettings()
        return BeautyConfig(ConstFilterArray, ConstFaceArray, settings)
    }

    private fun obtainBeautySettings(): BeautySettings? {
        return if (enginePrepared.get()) {
            mFURenderKit.faceBeauty?.let {
                BeautySettings(
                    matchFilterIndex(it.filterName),
                    it.filterIntensity,
                    mCurFaceIndex,
                    it.colorIntensity / FACE_COLOR_MULTIPLE,
                    it.blurIntensity / FACE_BLUR_MULTIPLE,
                    it.eyeEnlargingIntensityV2,
                    it.cheekThinningIntensity
                )
            }
        } else {
            null
        }
    }

    override fun releaseEngine() {
        synchronized(this) {
            writeLocalBeautySettings()
            enginePrepared.set(false)
            mFURenderKit.releaseSafe()
        }
    }

    private fun applyLocalConfig() {
        mFURenderKit.faceBeauty = FaceBeauty(FUBundleData(beautyBundleFile)).apply {
            readLocalBeautySettings().let {
                mCurFaceIndex = it.faceConfigIndex
                filterName = if (ConstFilterArray.size > it.filterIndex) {
                    ConstFilterArray[it.filterIndex].key
                } else FaceBeautyFilterEnum.ORIGIN
                filterIntensity = it.filterLevel

                blurType = FaceBeautyBlurTypeEnum.FineSkin
                colorIntensity = it.colorLevel * FACE_COLOR_MULTIPLE
                blurIntensity = it.blurLevel * FACE_BLUR_MULTIPLE
                eyeEnlargingIntensityV2 = it.eyeEnlarging
                cheekThinningIntensity = it.cheekThinning

                faceShapeIntensity = DEFAULT_FACE_SHAPE
                eyeBrightIntensity = DEFAULT_FACE_EYE_BRIGHT
                toothIntensity = DEFAULT_FACE_TOOTH
                cheekVIntensity = DEFAULT_FACE_CHEEK_V
                cheekSmallIntensityV2 = DEFAULT_FACE_CHEEK_SMALL
            }
        }
    }

    private fun writeLocalBeautySettings() {
        val beautySettings = obtainBeautySettings()?.toJson()
        IKLog.d(TAG, "writeLocalBeautySettings() json = $beautySettings")
        beautySettings?.let {
            sharedPreferences?.edit()?.putString("${KEY_BEAUTY_SETTINGS}_$uid", it)?.apply()
        }
    }

    private fun readLocalBeautySettings(): BeautySettings {
        val beautySettings = sharedPreferences?.getString("${KEY_BEAUTY_SETTINGS}_$uid", null)
        IKLog.d(TAG, "readLocalBeautySettings() json = $beautySettings")
        return beautySettings?.fromJson<BeautySettings>() ?: DefaultBeautySet
    }

    private fun matchFilterIndex(filterName: String): Int {
        val target = ConstFilterArray.find { it.key == filterName }
        return target?.number ?: 0
    }

    private fun setFUBeautyLogLevel(logLevel: Int) {
        val fuLogLevel = if (logLevel in 0..6)
            FULogger.LogLevel.values()[logLevel] else FULogger.LogLevel.ERROR
        setKitDebug(fuLogLevel)
        setCoreDebug(fuLogLevel)
    }
}