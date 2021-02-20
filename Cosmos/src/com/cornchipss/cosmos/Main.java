package com.cornchipss.cosmos;

import java.awt.Font;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import com.bulletphysics.linearmath.Transform;
import com.cornchipss.cosmos.biospheres.Biosphere;
import com.cornchipss.cosmos.biospheres.TestBiosphere;
import com.cornchipss.cosmos.blocks.Block;
import com.cornchipss.cosmos.blocks.BlockFace;
import com.cornchipss.cosmos.blocks.Blocks;
import com.cornchipss.cosmos.gui.GUI;
import com.cornchipss.cosmos.gui.GUIModel;
import com.cornchipss.cosmos.gui.GUITexture;
import com.cornchipss.cosmos.gui.GUITextureMultiple;
import com.cornchipss.cosmos.gui.text.GUIText;
import com.cornchipss.cosmos.gui.text.OpenGLFont;
import com.cornchipss.cosmos.physx.RayResult;
import com.cornchipss.cosmos.registry.Biospheres;
import com.cornchipss.cosmos.rendering.Texture;
import com.cornchipss.cosmos.rendering.Window;
import com.cornchipss.cosmos.shaders.Shader;
import com.cornchipss.cosmos.utils.Logger;
import com.cornchipss.cosmos.utils.Maths;
import com.cornchipss.cosmos.utils.io.Input;
import com.cornchipss.cosmos.world.ZaWARUDO;

public class Main
{
	private Window window;
	
	public static void main(String[] args)
	{
		new Main().run();
	}
	
	private void run()
	{
		Logger.LOGGER.setLevel(Logger.LogLevel.NONE);
		
		Blocks.init();
		
		Biospheres.registerBiosphere(TestBiosphere.class, "cosmos:test");
		
		window = new Window(1024, 720, "wack simulator 2021");
		
		Shader defaultShader = new Shader("assets/shaders/chunk");
		defaultShader.init();
		
		Texture mainTexture = Texture.loadTexture("atlas/main.png");
		
		Texture guiTexture = Texture.loadTexture("atlas/gui.png");
		
		ZaWARUDO world = new ZaWARUDO();
		
		GUI gui = new GUI(guiTexture);
		gui.init(window.getWidth(), window.getHeight());
		
		GUITexture crosshair = new GUITexture(new Vec3(), 0.1f, 0.1f, 0, 0);
		gui.addElement(crosshair);
		
		OpenGLFont font = new OpenGLFont(new Font("Arial", Font.PLAIN, 28));
		font.init();
		
		GUITextureMultiple[] inventorySlots = new GUITextureMultiple[10];
		
		GUIModel[] models = new GUIModel[10];
		
		int selectedSlot = 0;
		
		int slotDimensions = 64;
		
		int startX = (int)(1024 / 2.0f - (inventorySlots.length / 2.0f) * slotDimensions);
		
		for(int i = 0; i < models.length; i++)
		{
			inventorySlots[i] =  new GUITextureMultiple(
					new Vec3(startX + i * slotDimensions, 0, 0), slotDimensions, slotDimensions, 
					0.5f, 0,
					0, 0.5f);
			
			gui.addElement(inventorySlots[i]);
			
			if(i < Blocks.all().size())
			{
				int margin = 4;
				
				models[i] = new GUIModel(new Vec3(startX + i * slotDimensions + margin, margin, 0), 
						slotDimensions - margin * 2, Blocks.all().get(i).model(), mainTexture);
				
				gui.addElement(models[i]);
			}
		}
		
		inventorySlots[selectedSlot].state(1);
		
		GUIText fpsText = new GUIText("-- --ms", font, 0, 0);
		gui.addElement(fpsText);
		
		Structure s = new Structure(world, 100, 32, 100);
		s.init();
		
		Biosphere def = Biospheres.newInstance(Biospheres.getBiosphereIds().get(0));
		
		def.generatePlanet(s);
		
		s.calculateLights(false);
		
		for(Chunk c : s.chunks())
			c.render();
		
		Transform sT = new Transform();
		sT.setIdentity();
		sT.origin.set(0, 0, 0);
		
		s.addToWorld(sT);
		
		Ployer p = new Ployer(world);
		Transform playerTransform = new Transform();
		playerTransform.origin.set(0, s.height() / 2 + 3, 0);
		p.addToWorld(playerTransform);
		
		int timeLoc = defaultShader.uniformLocation("time");
		int camLoc = defaultShader.uniformLocation("u_camera");
		int transLoc = defaultShader.uniformLocation("u_transform");
		int projLoc = defaultShader.uniformLocation("u_proj");
		
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.perspective((float)Math.toRadians(90), 
				1024/720.0f,
				0.1f, 1000);
		
		Matrix4f guiProjMatrix = new Matrix4f();
		guiProjMatrix.perspective((float)Math.toRadians(90), 
				1024/720.0f,
				0.1f, 1000);
		
		Input.setWindow(window);
		
		long t = System.currentTimeMillis();
		
		final int UPS_TARGET = 70;
		
		final int MILLIS_WAIT = 1000 / UPS_TARGET;
		
		long lastSecond = t;
		
		int ups = 0;
		float variance = 0;
		
		Input.hideCursor(true);
		
		Input.update();
		
		boolean running = true;
		
		while(!window.shouldClose() && running)
		{
			if(window.wasWindowResized())
			{
				projectionMatrix.identity();
				projectionMatrix.perspective((float)Math.toRadians(90), 
						window.getWidth()/(float)window.getHeight(),
						0.1f, 1000);
				
				gui.updateProjection(window.getWidth(), window.getHeight());
			}
			
			float delta = System.currentTimeMillis() - t; 
			
			if(delta < MILLIS_WAIT)
			{
				try
				{
					Thread.sleep(MILLIS_WAIT - (int)delta);
					
					delta = (System.currentTimeMillis() - t);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			delta /= 1000.0f;
			
			if(delta > variance)
				variance = delta;
			
			t = System.currentTimeMillis();
			
			if(lastSecond / 1000 != t / 1000)
			{
				Logger.LOGGER.info("UPS: " + ups + "; Max Variance: " + variance*1000 + "ms");
				fpsText.text(ups + " " + (int)(variance*1000) + "ms");
				
				lastSecond = t;
				ups = 0;
				variance = 0;
			}
			ups++;
			
			if(Input.isKeyJustDown(GLFW.GLFW_KEY_F1))
				Input.toggleCursor();
			
			if(Input.isKeyDown(GLFW.GLFW_KEY_ESCAPE))
				running = false;
			
			world.update(delta);
			
			p.update(delta);
			
			for(int key = GLFW.GLFW_KEY_1; key <= GLFW.GLFW_KEY_9 + 1; key++)
			{
				if(key > GLFW.GLFW_KEY_9)
				{
					if(Input.isKeyJustDown(GLFW.GLFW_KEY_0))
					{
						inventorySlots[selectedSlot].state(0);
						selectedSlot = 9;
						inventorySlots[selectedSlot].state(1);
					}
				}
				else if(Input.isKeyJustDown(key))
				{
					inventorySlots[selectedSlot].state(0);
					selectedSlot = key - GLFW.GLFW_KEY_1;
					inventorySlots[selectedSlot].state(1);
					break;
				}
			}
			
			if(Input.isMouseBtnJustDown(GLFW.GLFW_MOUSE_BUTTON_1) || Input.isMouseBtnJustDown(GLFW.GLFW_MOUSE_BUTTON_2) || Input.isMouseBtnJustDown(GLFW.GLFW_MOUSE_BUTTON_3))
			{
				Vec3 from = p.camera().position();
				Vec3 dLook = Maths.mul(p.camera().forward(), 10.0f);
				Vec3 to = Maths.add(from, dLook);
				
				RayResult hits = s.shape().raycast(from.joml(), to.joml());
				
				if(hits.closestHit() != null)
				{
					Block selectedBlock = null;
					
					if(selectedSlot < Blocks.all().size())
						selectedBlock = Blocks.all().get(selectedSlot);
					
					Vector3i pos = new Vector3i(Maths.round(hits.closestHit().x()), 
							Maths.round(hits.closestHit().y()), 
							Maths.round(hits.closestHit().z()));
					
					if(Input.isMouseBtnJustDown(GLFW.GLFW_MOUSE_BUTTON_1))
					{
						s.block(pos.x, pos.y, pos.z, null);
					}
					else if(Input.isMouseBtnJustDown(GLFW.GLFW_MOUSE_BUTTON_2))
					{
						BlockFace face = hits.closestFace();
						
						int xx = Maths.floor(pos.x + 0.5f + (face.getRelativePosition().x * 2)), 
							yy = Maths.floor(pos.y + 0.5f + (face.getRelativePosition().y * 2)), 
							zz = Maths.floor(pos.z + 0.5f + (face.getRelativePosition().z * 2));
						
						if(s.withinBlocks(xx, yy, zz))
						{
							s.block(xx, yy, zz, selectedBlock);
						}
					}
					else if(Input.isMouseBtnJustDown(GLFW.GLFW_MOUSE_BUTTON_3))
					{
						s.beginBulkUpdate();
						
						final int radius = 20;
						
						Vec3 temp = new Vec3();
						Vec3 tempPos = new Vec3(pos.x, pos.y, pos.z);
						
						for(int dz = -radius; dz <= radius; dz++)
						{
							for(int dy = -radius; dy <= radius; dy++)
							{
								for(int dx = -radius; dx <= radius; dx++)
								{									
									int xx = pos.x + dx,
										yy = pos.y + dy, 
										zz = pos.z + dz;
									
									temp.x(xx);
									temp.y(yy);
									temp.z(zz);

									if(Maths.distSqrd(temp, tempPos) < radius * radius)
									{
										
										if(s.withinBlocks(xx, yy, zz))
										{
											s.block(xx, yy, zz, null);
										}
									}
								}
							}
						}
						
						s.endBulkUpdate();
					}
				}
			}
			
			GL11.glEnable(GL13.GL_TEXTURE0);
			
			render(delta);
			
			Input.update();

			defaultShader.use();
			defaultShader.setUniformF(timeLoc, (float)GLFW.glfwGetTime());
			mainTexture.bind();
			
			defaultShader.setUniformMatrix(projLoc, projectionMatrix);
			
			defaultShader.setUniformMatrix(camLoc, p.camera().viewMatrix());
			
			GL30.glEnable(GL30.GL_DEPTH_TEST);
			GL30.glDepthFunc(GL30.GL_LESS);
			
			//GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_LINE);
			
			for(Chunk chunk : s.chunks())
			{
				Matrix4f transform = new Matrix4f();
				Matrix4fc trans = s.transformMatrix();
				trans.mul(chunk.transformMatrix(), transform);
				
				defaultShader.setUniformMatrix(transLoc, transform);
				
				chunk.mesh().prepare();
				chunk.mesh().draw();
				chunk.mesh().finish();
			}
			
			Texture.unbind();
			
			defaultShader.stop();
			
			gui.draw();
			
			window.update();
		}
		
		window.destroy();
		
		Logger.LOGGER.info("Successfully closed.");
	}
	
	private void render(float delta)
	{
		window.clear(33 / 255.0f, 33 / 255.0f, 33 / 255.0f, 1.0f);
		
		// Render Code
	}
}