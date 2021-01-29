package com.appspell.shaderview

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.RawRes
import androidx.annotation.StyleRes
import com.appspell.shaderview.gl.GLQuadRender
import com.appspell.shaderview.gl.GLShader
import com.appspell.shaderview.gl.GLTextureView
import com.appspell.shaderview.gl.ShaderParams
import com.appspell.shaderview.log.LibLog

private const val OPENGL_VERSION = 3

private const val BIT_PER_CHANEL = 8
private const val DEPTH_BIT_PER_CHANEL = 16

private val DEFAULT_VERTEX_SHADER_RESOURCE = R.raw.quad_vert
private val DEFAULT_FRAGMENT_SHADER_RESOURCE = R.raw.default_frag

class ShaderView @JvmOverloads constructor(
    context: Context,
    @AttrRes attrs: AttributeSet? = null,
    @StyleRes defStyleAttr: Int = 0
) :
    GLTextureView(context, attrs, defStyleAttr),
    SurfaceTextureListener,
    View.OnLayoutChangeListener {

    @RawRes
    var vertexShaderRawResId: Int? = null
        set(value) {
            needToRecreateShaders = true
            field = value
        }

    @RawRes
    var fragmentShaderRawResId: Int? = null
        set(value) {
            needToRecreateShaders = true
            field = value
        }

    var shaderParams: ShaderParams? = null
        set(value) {
            field = value
            updateShaderParams()
        }
    var onViewReadyListener: ((shader: GLShader) -> Unit)? = null
    var onDrawFrameListener: ((shaderParams: ShaderParams) -> Unit)? = null

    private var needToRecreateShaders = false

    /**
     * Enable or disable logging for all of ShaderView globally
     * TODO it need to enable logs for this view only
     */
    var debugMode = false
        set(value) {
            field = value
            LibLog.isEnabled = value // TODO should be enabled for particular view only
            if (value) {
                setDebugFlags(DEBUG_CHECK_GL_ERROR.and(DEBUG_LOG_GL_CALLS))
                enableLogPauseResume = true
                enableLogEgl = true
                enableLogSurface = true
            }
        }

    /**
     * should we re-render this view all the time
     */
    var updateContinuously: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
            } else {
                setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
            }
        }

    private val renderer = GLQuadRender()

    private val rendererListener = object : GLQuadRender.ShaderViewListener {
        override fun onSurfaceCreated() {
            initShaders()
            onViewReadyListener?.invoke(renderer.shader)
        }

        override fun onDrawFrame(shaderParams: ShaderParams) {
            onDrawFrameListener?.invoke(shaderParams)
        }
    }

    init {
        initAttr(attrs)

        setEGLContextClientVersion(OPENGL_VERSION)
        renderer.listener = rendererListener
        setEGLConfigChooser(BIT_PER_CHANEL, BIT_PER_CHANEL, BIT_PER_CHANEL, BIT_PER_CHANEL, DEPTH_BIT_PER_CHANEL, 0)

        setRenderer(renderer)
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
    }

    private fun initAttr(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ShaderView, 0, 0)
            .apply {
                try {
                    fragmentShaderRawResId =
                        getResourceId(
                            R.styleable.ShaderView_fragment_shader_raw_res_id,
                            DEFAULT_FRAGMENT_SHADER_RESOURCE
                        )
                    vertexShaderRawResId =
                        getResourceId(R.styleable.ShaderView_vertex_shader_raw_res_id, DEFAULT_VERTEX_SHADER_RESOURCE)
                } finally {
                    recycle()
                }
            }
    }

    private fun updateShaderParams() {
        if (needToRecreateShaders) {
            return
        }
        shaderParams?.apply { renderer.shader.params = this }
    }

    private fun initShaders() {
        if (needToRecreateShaders) {
            fragmentShaderRawResId?.also { fragmentShader ->
                // delete existing shader is we have some
                renderer.shader.release()

                // create a new shader
                renderer.shader = renderer.shader.newBuilder()
                    .create(
                        context = context,
                        vertexShaderRawResId = vertexShaderRawResId ?: DEFAULT_VERTEX_SHADER_RESOURCE,
                        fragmentShaderRawResId = fragmentShader
                    )
                    .apply {
                        // if we have some ShaderParams to set
                        shaderParams?.apply { params(this) }
                    }
                    .build()
                    .also {
                        needToRecreateShaders = true
                    }
            }
        }

        // bind shader params.
        // note: we have to pass [android.content.res.Resources] to be able to load textures from Resources
        renderer.shader.bindParams(resources)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderer.shader.release()
        return super.onSurfaceTextureDestroyed(surface)
    }
}