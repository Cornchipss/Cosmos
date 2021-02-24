package com.cornchipss.cosmos.gui;

import org.joml.Matrix4f;
import org.joml.Vector3fc;

import com.cornchipss.cosmos.Mesh;
import com.cornchipss.cosmos.blocks.BlockFace;
import com.cornchipss.cosmos.models.CubeModel;
import com.cornchipss.cosmos.rendering.Texture;
import com.cornchipss.cosmos.utils.Maths;

public class GUIModel extends GUIElement
{
	private Mesh mesh;
	private Texture map;
	
	public GUIModel(Vector3fc position, float scale, CubeModel model, Texture map)
	{
		this(Maths.createTransformationMatrix(position, 0, 0, 0, scale), model, map);
	}
	
	public GUIModel(Matrix4f transform, CubeModel m, Texture map)
	{
		this(transform, m.createMesh(0, 0, -1, 1, BlockFace.FRONT), map);
	}
	
	public GUIModel(Vector3fc position, float scale, Mesh m, Texture map)
	{
		this(Maths.createTransformationMatrix(position, 0, 0, 0, scale), m, map);
	}
	
	public GUIModel(Matrix4f transform, Mesh m, Texture map)
	{
		super(transform);
		this.mesh = m;
		this.map = map;
	}
	
	@Override
	public Mesh guiMesh()
	{
		return mesh;
	}
	
	@Override
	public void prepare(GUI gui)
	{
		map.bind();
		super.prepare(gui);
	}
	
	@Override
	public void finish(GUI gui)
	{
		super.finish(gui);
		gui.texture().bind();
	}
}
