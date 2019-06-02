package cn.vecrates.videoeditdemo.shader;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author Vecrates.
 * @describe
 */
public class FormatDrawer extends BaseDrawer {

	private String vertextShaderCode;
	private String fragmentShaderCode;
	private int program;
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	private ShortBuffer vertexIndexBuffer;

	private int vertexCoordHandle;
	private int textureCoordHandle;
	private int textureHandle;
	private int vertexMatrixHandle;


	private float vertexCoords[] = {
			-1.0f, 1.0f,//lt
			-1.0f, -1.0f,
			1.0f, -1.0f,
			1.0f, 1.0f
	};

	private float textureCoords[] = {
			0.0f, 1.0f, //rb
			1.0f, 1.0f,
			1.0f, 0.0f,
			0.0f, 0.0f
	};

	private short[] vertexIndexs = {0, 1, 2, 0, 2, 3};

	private float[] vertexMatrix = new float[16];

	public FormatDrawer() {
		vertextShaderCode = readShaderFile("format_vs.glsl");
		fragmentShaderCode = readShaderFile("format_fs.glsl");
		initShader();
		initCoords();
	}

	private void initShader() {
		program = GLES20.glCreateProgram();
		int vertextShader = loadShader(GLES20.GL_VERTEX_SHADER, vertextShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		GLES20.glAttachShader(program, vertextShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
		GLES20.glDeleteShader(vertextShader);
		GLES20.glDeleteShader(fragmentShader);
	}

	private void initCoords() {
		//顶点
		vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.length * 4)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertexCoords);
		vertexBuffer.position(0);

		//纹理
		textureBuffer = ByteBuffer.allocateDirect(vertexCoords.length * 4)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(textureCoords);
		textureBuffer.position(0);

		vertexIndexBuffer = ByteBuffer.allocateDirect(vertexIndexs.length * 2)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer()
				.put(vertexIndexs);
		vertexIndexBuffer.position(0);

		Matrix.setIdentityM(vertexMatrix, 0);

		//顶点坐标句柄
		vertexCoordHandle = GLES20.glGetAttribLocation(program, "vPosition");
		//纹理坐标句柄
		textureCoordHandle = GLES20.glGetAttribLocation(program, "vCoord");
		//顶点矩阵
		vertexMatrixHandle = GLES20.glGetUniformLocation(program, "vMatrix");
		//纹理
		textureHandle = GLES20.glGetUniformLocation(program, "vTexture");
	}

	public void draw(int texture) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		//激活这个程序,在glUseProgram函数调用之后，每个着色器调用和渲染调用都会使用这个程序对象
		GLES20.glUseProgram(program);

		GLES20.glEnableVertexAttribArray(vertexCoordHandle);
		GLES20.glEnableVertexAttribArray(textureCoordHandle);

		//激活绑定纹理
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

		//传入顶点坐标
		vertexBuffer.position(0);
		GLES20.glVertexAttribPointer(vertexCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * Float.BYTES, vertexBuffer);
		//传入纹理坐标
		textureBuffer.position(0);
		GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * Float.BYTES, textureBuffer);

		GLES20.glUniformMatrix4fv(vertexMatrixHandle, 1, false, vertexMatrix, 0);

		GLES20.glUniform1i(textureHandle, 0);

		//三角形,顶点个数,索引类型,索引缓冲区（不使用0）
		vertexIndexBuffer.position(0);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, vertexIndexBuffer);
		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
		GLES20.glDisableVertexAttribArray(textureCoordHandle);

		int code = GLES20.glGetError();
		if (code != 0) {
			Log.e("FormatDrawer", "error code=" + code);
		}

	}


	private int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}


}
