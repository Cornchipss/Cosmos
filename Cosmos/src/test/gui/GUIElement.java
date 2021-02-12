package test.gui;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.cornchipss.utils.Maths;

import test.Mesh;
import test.Vec3;

public abstract class GUIElement
{
	protected Matrix4f transform;
	
	public GUIElement(Matrix4fc transform)
	{
		this.transform = new Matrix4f().set(transform);
	}
	
	public GUIElement(Vec3 position, float rx, float ry, float rz, float scale)
	{
		transform = Maths.createTransformationMatrix(position, rx, ry, rz, scale);
	}
	
	public GUIElement(Vec3 position, float scale)
	{
		this(position, 0, 0, 0, 1);
	}
	
	public GUIElement(Vec3 position)
	{
		this(position, 1);
	}
	
	public void prepare(GUI gui)
	{
		guiMesh().prepare();
	}
	
	public void draw(GUI gui)
	{
		guiMesh().draw();
	}
	
	public void finish(GUI gui)
	{
		guiMesh().finish();
	}
	
	public abstract Mesh guiMesh();

	public Matrix4fc transform()
	{
		return transform;
	}
}
