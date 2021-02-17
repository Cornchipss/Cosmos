package test.gui;

import java.awt.Font;
import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

import com.cornchipss.rendering.Texture;

import test.gui.text.TextRenderer;
import test.shaders.Shader;

public class GUI
{
	private List<GUIElement> elements;
	
	private Texture texture;
	
	private Shader shader;
	
	private Matrix4f projectionMatrix;
	
	private int guiTransLoc;
	private int guiProjLoc;
	
	private TextRenderer textRenderer;
	
	public GUI(Texture texture)
	{
		this.texture = texture;
		elements = new LinkedList<>();
	}
	
	public void init(int width, int height)
	{
		shader = new Shader("assets/shaders/gui");
		shader.init();
		
		updateProjection(width, height);
		
		guiTransLoc = shader.uniformLocation("u_transform");
		guiProjLoc = shader.uniformLocation("u_projection");
		
		textRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 72));
	}
	
	public void draw()
	{
		shader.use();
		
		GL30.glDisable(GL30.GL_DEPTH_TEST);
		
		shader.setUniformMatrix(guiProjLoc, projectionMatrix);
		
		texture.bind();
		
		for(GUIElement e : elements)
		{
			shader.setUniformMatrix(guiTransLoc, e.transform());
			
			e.prepare(this);
			e.draw(this);
			e.finish(this);
		}
		
		shader.setUniformMatrix(guiTransLoc, new Matrix4f().identity());
		
		shader.setUniformMatrix(guiProjLoc, new Matrix4f().ortho2D(0, 1024, 0, 720));
		
		textRenderer.renderText("Hello, World!", 1024 / 2 - 200, 200);
		
		shader.stop();
	}
	
	public Shader shader()
	{
		return shader;
	}
	
	public Texture texture()
	{
		return texture;
	}
	
	public void addElement(GUIElement... e)
	{
		for(GUIElement elem : e)
		{
			if(elem == null)
				throw new IllegalArgumentException("Attempt to add a null GUI element");
			elements.add(elem);
		}
	}
	
	public void removeElement(GUIElement e)
	{
		elements.remove(e);
	}

	public void updateProjection(int width, int height)
	{
		if(projectionMatrix == null)
			projectionMatrix = new Matrix4f();
		projectionMatrix.identity();
		projectionMatrix.perspective((float)Math.toRadians(90), 
				width/(float)height,
				0.1f, 1000);
	}
}
