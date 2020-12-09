package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.cornchipss.rendering.Texture;
import com.cornchipss.rendering.Window;
import com.cornchipss.utils.Input;
import com.cornchipss.utils.Maths;
import com.cornchipss.utils.Utils;

import test.models.CubeModel;
import test.models.DirtModel;
import test.models.GrassModel;
import test.models.StoneModel;

public class Main
{
	private Window window;
	
	/**
	 * Loads the shaders + returns the program ID they are linked to
	 * @return
	 */
	private int loadShaders()
	{
		StringBuilder shaderCode = new StringBuilder();
		
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader("./assets/shaders/test.vert"));
			
			for(String line = br.readLine(); line != null; line = br.readLine())
			{
				shaderCode.append(line + System.lineSeparator());
			}
			
			br.close();
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
		
		int vertexShader = GL30.glCreateShader(GL30.GL_VERTEX_SHADER);
		GL30.glShaderSource(vertexShader, shaderCode.toString());
		GL30.glCompileShader(vertexShader);
		
		int success = GL30.glGetShaderi(vertexShader, GL30.GL_COMPILE_STATUS);
		if(success == 0)
		{
			String log = GL30.glGetShaderInfoLog(vertexShader);
			System.err.println("Vertex Shader Compilation Error!!!");
			System.err.print(log);
			System.exit(-1);
		}
		
		// Fragment Shader
		
		shaderCode = new StringBuilder();
		try
		{
			br = new BufferedReader(new FileReader("./assets/shaders/test.frag"));
			
			for(String line = br.readLine(); line != null; line = br.readLine())
			{
				shaderCode.append(line + System.lineSeparator());
			}
			
			br.close();
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
		int fragShader = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER);
		GL30.glShaderSource(fragShader, shaderCode.toString());
		GL30.glCompileShader(fragShader);
		
		success = GL30.glGetShaderi(fragShader, GL30.GL_COMPILE_STATUS);
		if(success == 0)
		{
			String log = GL30.glGetShaderInfoLog(fragShader);
			System.err.println("Fragment Shader Compilation Error!!!");
			System.err.print(log);
			System.exit(-1);
		}
		
		
		int shaderProgram = GL30.glCreateProgram();
		GL30.glAttachShader(shaderProgram, vertexShader);
		GL30.glAttachShader(shaderProgram, fragShader);
		GL30.glLinkProgram(shaderProgram);
		GL20.glValidateProgram(shaderProgram);
		
		System.out.println("Shader Loader > " + GL30.glGetProgramInfoLog(shaderProgram));
		
		if(GL30.glGetProgrami(shaderProgram, GL30.GL_LINK_STATUS) == 0)
		{
			String log = GL30.glGetProgramInfoLog(shaderProgram);
			System.err.println("Shader Program Linking Error!!!");
			System.err.print(log);
			System.exit(-1);
		}
		
		// Once they are linked to the program, we do not need them anymore.
		GL30.glDeleteShader(vertexShader);
		GL30.glDeleteShader(fragShader);
		
		return shaderProgram;
	}
	
	public static void main(String[] args)
	{
		new Main().run();
	}

	private void run()
	{
		window = new Window(1024, 720, "mgay");
		
		int shaderProgram = loadShaders();
				
//		float[] vertices = {// first triangle
//			     0.5f,  0.5f, 0.0f,  // top right
//			     0.5f, -0.5f, 0.0f,  // bottom right
//			    -0.5f,  0.5f, 0.0f,  // top left 
//			    -0.5f, -0.5f, 0.0f,  // bottom left
//		};
		
		float[] vertices = {// first triangle
				// front
			    -0.5f, -0.5f,  0.5f,
			     0.5f, -0.5f,  0.5f,
			     0.5f,  0.5f,  0.5f,
			    -0.5f,  0.5f,  0.5f,
			    // back
			    -0.5f, -0.5f, -0.5f,
			     0.5f, -0.5f, -0.5f,
			     0.5f,  0.5f, -0.5f,
			    -0.5f,  0.5f, -0.5f
		};
		
		int[] indices = 
			{
					// front
					0, 1, 2,
					2, 3, 0,
					// right
					1, 5, 6,
					6, 2, 1,
					// back
					7, 6, 5,
					5, 4, 7,
					// left
					4, 0, 3,
					3, 7, 4,
					// bottom
					4, 5, 1,
					1, 0, 4,
					// top
					3, 2, 6,
					6, 7, 3
			};
		
		float[] cols = 
			{
				1.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0,0f
			};
		
		
		BulkModel beeg = new BulkModel(100, 100, 100);
		
		CubeModel grass = new GrassModel();
		CubeModel dirt  = new DirtModel();
		CubeModel stone = new StoneModel();
		Random rdm = new Random();
		
		for(int z = 0; z < 50; z++)
		{
			for(int x = 0; x < 50; x++)
			{
				int yEnd = rdm.nextInt(2) + 30;
				for(int y = 0; y < 1; y++)
				{
					if(y == yEnd - 1)
						beeg.setModel(x, y, z, grass);
					else if(y > yEnd)
						beeg.setModel(x, y, z, dirt);
					else
						beeg.setModel(x, y, z, stone);
				}
			}
		}
		
		beeg.render();
		
		int timeLoc = GL20.glGetUniformLocation(shaderProgram, "time");
		int camLoc = GL20.glGetUniformLocation(shaderProgram, "u_camera");
		int transLoc = GL20.glGetUniformLocation(shaderProgram, "u_transform");
		int projLoc = GL20.glGetUniformLocation(shaderProgram, "u_proj");
		
		Matrix4f cameraMatrix = new Matrix4f();
		
		Matrix4f meshMatrix = new Matrix4f();
		
		meshMatrix.translate(new Vector3f(0, 0, -20f));
		
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.perspective((float)Math.toRadians(90), 
				1024/720.0f,
				0.1f, 1000);
		
		Texture tex = Texture.loadTexture("atlas/main.png");
		
		Utils.println(projectionMatrix);
		
		Input.setWindow(window);
		
		Vector3f pos = new Vector3f();
		Vector3f rot = new Vector3f();
		
		int i = 0;
		
		while(!window.shouldClose())
		{
			update();
			
			if(++i == 1000)
			{
				i = 0;
				beeg.render();
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			GL11.glEnable(GL13.GL_TEXTURE0);
			
			render();
			
			GL30.glUseProgram(shaderProgram);
			GL20.glUniform1f(timeLoc, (float)GLFW.glfwGetTime());
			tex.bind();
			
			GL20.glUniformMatrix4fv(projLoc, false, projectionMatrix.get(new float[16]));
			
			if(Input.isKeyDown(GLFW.GLFW_KEY_W))
				pos.z -= 0.1f;
			if(Input.isKeyDown(GLFW.GLFW_KEY_S))
				pos.z += 0.1f;
			if(Input.isKeyDown(GLFW.GLFW_KEY_D))
				pos.x += 0.1f;
			if(Input.isKeyDown(GLFW.GLFW_KEY_A))
				pos.x -= 0.1f;
			if(Input.isKeyDown(GLFW.GLFW_KEY_E))
				pos.y += 0.1f;
			if(Input.isKeyDown(GLFW.GLFW_KEY_Q))
				pos.y -= 0.1f;
			
			if(Input.isKeyDown(GLFW.GLFW_KEY_C))
				rot.y += 0.01f;
			if(Input.isKeyDown(GLFW.GLFW_KEY_Z))
				rot.y -= 0.01f;
			
			if(Input.isKeyDown(GLFW.GLFW_KEY_R))
				rot.x += 0.01f;
			if(Input.isKeyDown(GLFW.GLFW_KEY_T))
				rot.x -= 0.01f;
			
			Maths.createViewMatrix(pos, rot, cameraMatrix);
			
			GL20.glUniformMatrix4fv(camLoc, false, cameraMatrix.get(new float[16]));
			
			GL20.glUniformMatrix4fv(transLoc, false, meshMatrix.get(new float[16]));
			
			GL30.glEnable(GL30.GL_DEPTH_TEST);
			GL30.glDepthFunc(GL30.GL_LESS);
			
			GL30.glBindVertexArray(beeg.beeg.vao());
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);
			GL11.glDrawElements(GL20.GL_TRIANGLES, beeg.beeg.verticies(), GL11.GL_UNSIGNED_INT, 0);
			GL20.glDisableVertexAttribArray(2);
			GL20.glDisableVertexAttribArray(1);
			GL20.glDisableVertexAttribArray(0);
			GL30.glBindVertexArray(0);
			
			Texture.unbind();
			
			window.update();

		}
		
		window.destroy();
	}
	
	private void update()
	{
	}
	
	private void render()
	{
		window.clear(33 / 255.0f, 33 / 255.0f, 33 / 255.0f, 1.0f);
		
		// Render Code
	}
}