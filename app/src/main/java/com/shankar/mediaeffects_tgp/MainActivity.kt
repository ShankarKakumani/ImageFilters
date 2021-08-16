package com.shankar.mediaeffects_tgp

import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() , GLSurfaceView.Renderer{


    private lateinit var surfaceView: GLSurfaceView
    private val renderer = TextureRenderer()
    private val textures = IntArray(3)
    private var effectContext: EffectContext? = null
    private var effect: Effect? = null

    private var mImageWidth = 0
    private var mImageHeight = 0
    private var mInitialized = false
    private val mCurrentEffect = 0

    val mEffectArray: ArrayList<Effect> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initViews()
    }

    private fun initViews() {
        surfaceView = findViewById(R.id.surface_view)
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setRenderer(this)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Nothing to do here
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        renderer.updateViewSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!mInitialized) {
            //Only need to do this once
            effectContext = EffectContext.createWithCurrentGlContext()
            renderer.init()
            loadTextures()
            mInitialized = true
        }
        if (mCurrentEffect != R.id.none) {
            //if an effect is chosen initialize it and apply it to the texture
            initEffect()
//            applyEffect()
            applyMEffect()
        }
        renderResult()
    }


    private fun loadTextures() {
        // Generate textures
        GLES20.glGenTextures(3, textures, 0)

        // Load input bitmap
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.puppy)
        mImageWidth = bitmap.width
        mImageHeight = bitmap.height
        renderer.updateTextureSize(mImageWidth, mImageHeight)

        // Upload to texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        // Set texture parameters
        GLToolbox.initTexParams()
    }

    private fun initEffect() {
        val effectFactory: EffectFactory = effectContext!!.factory
        mEffectArray.clear()

        val saturate = effectFactory.createEffect(EffectFactory.EFFECT_SATURATE)
        saturate.setParameter("scale", 1.5f)

        val brightness = effectFactory.createEffect(EffectFactory.EFFECT_BRIGHTNESS)
        brightness.setParameter("brightness", 0.5f)

        val vignette = effectFactory.createEffect(EffectFactory.EFFECT_VIGNETTE)
        vignette.setParameter("scale", 0.6f)

        val contrast = effectFactory.createEffect(EffectFactory.EFFECT_CONTRAST)
        contrast.setParameter("contrast", 1.4f)

        val temp = effectFactory.createEffect(EffectFactory.EFFECT_TEMPERATURE)
        temp.setParameter("scale", .9f)

        val hue = effectFactory.createEffect(EffectFactory.EFFECT_TINT)
        hue.setParameter("tint", getHueColor(0, HueType.RED.value))

//        mEffectArray.add(brightness)
//        mEffectArray.add(temp)
        mEffectArray.add(contrast)
//        mEffectArray.add(saturate)
//        mEffectArray.add(hue)
//        mEffectArray.add(vignette)

    }


    private fun applyEffect() {
        effect?.apply(textures[0], mImageWidth, mImageHeight, textures[1])
    }

    private fun renderResult() {
        renderer.renderTexture(textures[2])
        renderer.renderTexture(textures[1])
    }


    private fun applyMEffect() {
        if (mEffectArray.size > 0) { // if there is any effect
            mEffectArray[0].apply(
                textures[0],
                mImageWidth,
                mImageHeight,
                textures[1]
            ) // apply first effect
            for (i in 1 until mEffectArray.size) { // if more that one effect
                val sourceTexture: Int = textures[1]
                val destinationTexture: Int = textures[2]
                mEffectArray[i].apply(sourceTexture, mImageWidth, mImageHeight, destinationTexture)
                textures[1] =
                    destinationTexture // changing the textures array, so 1 is always the texture for output,
                textures[2] = sourceTexture // 2 is always the sparse texture
            }
        }
    }

    private fun getHueColor(alpha: Int, hueType: Int): Int {
        return when (hueType) {

            HueType.NONE.value -> {
                Color.argb(0, 0, 0, 0)
            }
            HueType.YELLOW.value -> {
                Color.argb(alpha, 255, 243, 0)
            }
            HueType.GOLD.value -> {
                Color.argb(alpha, 249, 196, 6)
            }
            HueType.RED.value -> {
                Color.argb(alpha, 215, 1, 1)
            }
            HueType.MAGENTA.value -> {
                Color.argb(alpha, 215, 1, 198)
            }
            HueType.VIOLET.value -> {
                Color.argb(alpha, 85, 1, 215)
            }
            HueType.BLUE.value -> {
                Color.argb(alpha, 1, 34, 215)
            }
            HueType.SKYBLUE.value -> {
                Color.argb(alpha, 6, 206, 249)
            }
            HueType.GREEN.value -> {
                Color.argb(alpha, 16, 213, 0)
            }
            else -> {
                Color.argb(0, 0, 0, 0)
            }
        }

    }

    enum class HueType(val value: Int) {
        NONE(0),
        YELLOW(1),
        GOLD(2),
        RED(3),
        MAGENTA(4),
        VIOLET(5),
        BLUE(6),
        SKYBLUE(7),
        GREEN(8),
    }

}