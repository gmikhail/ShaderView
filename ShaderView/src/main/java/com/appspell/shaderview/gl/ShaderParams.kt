package com.appspell.shaderview.gl

import android.opengl.GLES20
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*

const val UNKNOWN_LOCATION = -1

class ShaderParams {
    private data class Attr(
        val type: AttrType,
        var location: Int = UNKNOWN_LOCATION,
        var value: Any
    )

    enum class AttrType {
        FLOAT, INT,
        FLOAT_VEC2, FLOAT_VEC3, FLOAT_VEC4,
        INT_VEC2, INT_VEC3, INT_VEC4,
        BOOL,
        SAMPLER_2D
    }

    private val map = HashMap<String, Attr>()

    fun updateValue(attrName: String, value: Float) {
        map[attrName]?.value = value
    }

    fun updateValue(attrName: String, value: Int) {
        map[attrName]?.value = value
    }

    fun updateValue(attrName: String, value: Boolean) {
        map[attrName]?.value = value
    }

    fun updateValue(attrName: String, value: FloatArray) {
        map[attrName]?.value = value
    }

    fun updateValue(attrName: String, value: IntArray) {
        map[attrName]?.value = value
    }

    fun updateAttrLocation(attrName: String, shaderProgram: Int) {
        map[attrName]?.apply {
            this.location = GLES20.glGetUniformLocation(shaderProgram, attrName)
        }
    }

    fun bindAttrs(shaderProgram: Int) {
        for (key in map.keys) {
            updateAttrLocation(key, shaderProgram)
        }
    }

    fun pushValuesToProgram() {
        for (key in map.keys) {
            val attr = map[key]
            if (attr == null || attr.location == UNKNOWN_LOCATION) {
                continue
            }
            when (attr.type) {
                AttrType.FLOAT -> GLES20.glUniform1f(attr.location, attr.value as Float)
                AttrType.INT -> GLES20.glUniform1i(attr.location, attr.value as Int)
                AttrType.FLOAT_VEC2 -> GLES20.glUniform2fv(attr.location, 1, (attr.value as FloatArray), 0)
                AttrType.FLOAT_VEC3 -> GLES20.glUniform3fv(attr.location, 1, (attr.value as FloatArray), 0)
                AttrType.FLOAT_VEC4 -> GLES20.glUniform4fv(attr.location, 1, (attr.value as FloatArray), 0)
                AttrType.INT_VEC2 -> GLES20.glUniform2iv(attr.location, 1, (attr.value as IntArray), 0)
                AttrType.INT_VEC3 -> GLES20.glUniform3iv(attr.location, 1, (attr.value as IntArray), 0)
                AttrType.INT_VEC4 -> GLES20.glUniform4iv(attr.location, 1, (attr.value as IntArray), 0)
                AttrType.BOOL -> GLES20.glUniform1i(attr.location, if (attr.value as Boolean) 1 else 0)
                AttrType.SAMPLER_2D -> TODO()
            }
        }
    }

    class Builder {
        private val result = ShaderParams()

        fun add(attrName: String, value: Float): Builder {
            val attr = Attr(type = AttrType.FLOAT, value = value)
            result.map[attrName] = attr
            return this
        }

        fun add(attrName: String, value: Int): Builder {
            val attr = Attr(type = AttrType.INT, value = value)
            result.map[attrName] = attr
            return this
        }

        fun add(attrName: String, value: Boolean): Builder {
            val attr = Attr(type = AttrType.BOOL, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec2(attrName: String, value: FloatArray): Builder {
            val attr = Attr(type = AttrType.FLOAT_VEC2, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec3(attrName: String, value: FloatArray): Builder {
            val attr = Attr(type = AttrType.FLOAT_VEC3, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec4(attrName: String, value: FloatArray): Builder {
            val attr = Attr(type = AttrType.FLOAT_VEC4, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec2(attrName: String, value: IntArray): Builder {
            val attr = Attr(type = AttrType.INT_VEC2, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec3(attrName: String, value: IntArray): Builder {
            val attr = Attr(type = AttrType.INT_VEC3, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec4(attrName: String, value: IntArray): Builder {
            val attr = Attr(type = AttrType.INT_VEC4, value = value)
            result.map[attrName] = attr
            return this
        }

        fun build() = result
    }
}
