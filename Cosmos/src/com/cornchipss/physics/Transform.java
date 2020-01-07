package com.cornchipss.physics;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import com.cornchipss.utils.Maths;
import com.cornchipss.utils.Utils;

public class Transform
{
	private Matrix4f rotationX = new Matrix4f().identity(), rotationY = new Matrix4f().identity(), rotationZ = new Matrix4f().identity();
	
	private Vector3f position;
	
	private Quaternionf rotation;
	private Vector3f eulers = Maths.zero();
	
	private Vector3f velocity = Maths.zero();
	
	private Matrix4f transform = new Matrix4f();
	
	public Transform()
	{
		this(Maths.zero());
	}
	
	public Transform(Vector3fc position)
	{
		this(position, new Quaternionf());
	}
	
	public Transform(Vector3fc position, Quaternionf rotation)
	{
		transform = new Matrix4f();
		
		setPosition(new Vector3f(position));
		setRotation(rotation);
	}
	
	/**
	 * Combines two other transforms without modifying either one<br>
	 * Equivilant to <code>return this + other</code> - make sure it's parent + child - not the other way around
	 * @param child The transform to combine this with
	 * @return Combination of two other transforms without modifying either one
	 */
	public Transform combine(Transform child)
	{
		Transform t = new Transform(
				Maths.add(getPosition(), Maths.rotatePoint(getCombinedRotation(), child.getPosition())), 
				Maths.mul(child.rotation, rotation));
		t.setVelocity(Maths.add(child.velocity, velocity));
		return t;
	}
	
	/**
	 * Separates two other transforms without modifying either one<br>
	 * The inverse of {@link Transform#combine(Transform)}<br>
	 * Equivilant to <code>return this - other</code> - make sure it's parent - child - not the other way around
	 * @param child The transform to seperate this from
	 * @return Separation of two other transforms without modifying either one
	 */
	public Transform separate(Transform child)
	{
		Transform t = new Transform(
				Maths.sub(Maths.rotatePoint(getCombinedRotation().invert(), child.getPosition()), getPosition()), 
				Maths.div(child.getRotation(), getRotation())); // correct rotation
		t.setVelocity(Maths.sub(child.velocity, velocity));
		return t;
	}
	
	public Vector3f getPosition() { return position; }
	public void setPosition(Vector3fc position)
	{
		this.position = new Vector3f(position);
		transform.transform(new Vector4f(position, 0));
	}

	public void setX(float x) { position.x = x; }
	public void setY(float y) { position.y = y; }
	public void setZ(float z) { position.z = z; }
	
	public float getX() { return position.x; }
	public float getY() { return position.y; }
	public float getZ() { return position.z; }
	
	public void translate(Vector3fc amt)
	{
		setX(amt.x() + getX());
		setY(amt.y() + getY());
		setZ(amt.z() + getZ());
	}
	
	public Vector3fc getEulers() { return eulers; }
	public Quaternionf getRotation() { return rotation; }
	public void setRotation(Quaternionf rotation)
	{
		Vector3f prevEulers = new Vector3f(getEulers());
		
		this.rotation = rotation;
		rotation.getEulerAnglesXYZ(eulers);
		
		/*
		 * https://open.gl/transformations
		 */
		
		if(this.rotation == null || prevEulers.x != eulers.x)
			rotationX = Maths.createRotationMatrix(Utils.x(), eulers.x);
		if(this.rotation == null || prevEulers.y != eulers.y)
			rotationY = Maths.createRotationMatrix(Utils.y(), eulers.y);
		if(this.rotation == null || prevEulers.z != eulers.z)
			rotationZ = Maths.createRotationMatrix(Utils.z(), eulers.z);
	}
	public void setRotation(float rx, float ry, float rz)
	{
		setRotation(Maths.quaternionFromRotation(rx, ry, rz));
	}
	
	public void rotate(float rx, float ry, float rz)
	{
		rotation.rotateXYZ(rx, ry, rz);
		setRotation(rotation);
	}
	
	public void rotate(Vector3fc delta)
	{
		rotate(delta.x(), delta.y(), delta.z());
	}

	public void setRotationX(float rx)
	{
		setRotation(rx, getRotationY(), getRotationZ());
	}
	
	public void setRotationY(float ry)
	{
		setRotation(getRotationX(), ry, getRotationZ());
	}
	
	public void setRotationZ(float rz)
	{
		setRotation(getRotationX(), getRotationY(), rz);
	}
	
	/**
	 * Gets the velocity
	 * @return the velocity
	 */
	public Vector3f getVelocity() { return velocity; }
	
	/**
	 * Stores velocity - does not change the position
	 * @param v the velocity value to store
	 */
	public void setVelocity(Vector3f v) { velocity = v; }
	
	/**
	 * Adds the the velocity
	 * @param amt The amount to add to the velocity
	 */
	public void accelerate(Vector3fc amt)
	{
		velocity.x += amt.x();
		velocity.y += amt.y();
		velocity.z += amt.z();
	}
	
	/**
	 * Gets the rotation in the X direction
	 * @return the rotation in the X direction 
	 */
	public float getRotationX() { return eulers.x(); }
	
	/**
	 * Gets the rotation in the X direction
	 * @return the rotation in the X direction 
	 */
	public float getRotationY() { return eulers.y(); }
	
	/**
	 * Gets the rotation in the Y direction
	 * @return the rotation in the Y direction 
	 */
	public float getRotationZ() { return eulers.z(); }
	
	/**
	 * Gets the rotation matrix for the Z axis
	 * @return The rotation matrix for the Z axis
	 */
	public Matrix4f getRotationMatrixX() { return rotationX; }
	
	/**
	 * Gets the rotation matrix for the Y axis
	 * @return The rotation matrix for the Y axis
	 */
	public Matrix4f getRotationMatrixY() { return rotationY; }
	
	/**
	 * Gets the rotation matrix for the Z axis
	 * @return The rotation matrix for the Z axis
	 */
	public Matrix4f getRotationMatrixZ() { return rotationZ; }
	
	/**
	 * A combination of {@linkplain Transform#getRotationMatrixX()} * {@linkplain Transform#getRotationMatrixY()} * {@linkplain Transform#getRotationMatrixZ()}
	 * @return A copy of the combination of the 3 rotation vectors
	 */
	public Matrix4f getCombinedRotation()
	{
		return Maths.mul(rotationX, rotationY).mul(rotationZ);
	}
}
