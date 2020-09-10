package com.cornchipss.physics;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.cornchipss.utils.Maths;

public class Transform
{
	private Vector3f localPosition;
	
	private Vector3f localVelocity;
	
	private Quaternionf localRotation;
	
	private Transform parent;
	
	public Transform()
	{
		this(Maths.zero());
	}
	
	public Transform(Vector3fc loc)
	{
		this(loc, Maths.blankQuaternion());
	}
	
	public Transform(Vector3fc loc, Vector3fc rot)
	{
		this(loc, Maths.quaternionFromRotation(rot));
	}
	
	public Transform(Vector3fc loc, Quaternionfc quat)
	{
		localPosition = new Vector3f(loc);
		localRotation = Maths.clone(quat);
		localVelocity = Maths.zero();
	}
	
	public Transform(Transform parent)
	{
		this.parent = parent;
		
		this.localPosition = new Vector3f();
		this.localRotation = new Quaternionf();
		this.localVelocity = new Vector3f();
	}
	
	public boolean hasParent()
	{
		return parent != null;
	}
	
	public void removeParent()
	{
		if(hasParent())
		{
			localPosition.set(position());
			localRotation.set(rotation());
			localVelocity.set(velocity());
			
			this.parent = null;
		}
	}
	
	public void parent(Transform p)
	{
		removeParent();
				
		this.parent = p;
		
		if(p != null)
		{
			// These now represent differing things
			localPosition = Maths.sub(localPosition(), parent.position());
			localRotation = Maths.div(localRotation(), parent.rotation());
			localVelocity = Maths.sub(localVelocity(), parent.velocity());
		}
	}
	
	public Transform parent()
	{
		return parent;
	}
	
	public Vector3fc velocity()
	{
		if(hasParent())
		{
			Vector3fc parentVel = parent().velocity();
			return Maths.add(parentVel, localVelocity());
		}
		
		return localVelocity();
	}
	
	public void velocity(Vector3fc vel)
	{
		accelerate(Maths.sub(vel, velocity()));
	}
	
	public void accelerate(Vector3fc accel)
	{
		localVelocity.add(accel);
	}
	
	public Quaternionfc localRotation()
	{
		return localRotation;
	}
	
	public void localRotation(float x, float y, float z)
	{
		localRotation(Maths.quaternionFromRotation(x, y, z));
	}
	
	public void localRotation(Vector3fc rot)
	{
		localRotation(Maths.quaternionFromRotation(rot));
	}
	
	public void localRotation(Quaternionfc rot)
	{
		localRotation = Maths.clone(rot);
	}
	
	public Quaternionfc rotation()
	{
		if(hasParent())
		{
			Quaternionfc parentRot = parent().rotation();
			return localRotation.mul(parentRot, new Quaternionf());
//			return localRotation();
		}
		else
			return localRotation();
	}
	
	public void rotation(Vector3fc rot)
	{
		rotation(Maths.quaternionFromRotation(rot));
	}
	
	public void rotation(float x, float y, float z)
	{
		rotation(Maths.quaternionFromRotation(x, y, z));
	}
	
	/**
	 * Sets the rotation of this transform
	 * @param rot The rotation to set it to
	 */
	public void rotation(Quaternionfc rot)
	{
		Quaternionf delta = rot.div(rotation(), new Quaternionf());
		delta.normalize();
		localRotation.mul(delta);
	}
	
	public void position(float x, float y, float z)
	{
		position(new Vector3f(x, y, z));
	}
	
	public void position(Vector3fc pos)
	{
		Vector3f delta = Maths.sub(pos, position());
		
		localPosition.add(delta);
	}
	
	public Vector3fc position()
	{
		if(hasParent())
		{
			Vector3fc parentPos = parent().position();
			Quaternionfc parentRot = parent().rotation();
			
			Matrix4f parentTransform = Maths.createTransformationMatrix(parentPos, parentRot);
			return Maths.rotatePoint(parentTransform, localPosition()).add(parentPos);
		}
		
		return localPosition();
	}
	
	public void localPosition(float x, float y, float z)
	{
		localPosition(new Vector3f(x, y, z));
	}
	
	public void localPosition(Vector3fc pos)
	{
		this.localPosition = new Vector3f(pos);
	}
	
	public Vector3fc localPosition()
	{
		return localPosition;
	}
	
	public void localVelocity(Vector3fc vel)
	{
		this.localVelocity = new Vector3f(vel);
	}
	
	public Vector3fc localVelocity()
	{
		return localVelocity;
	}
	
	public void translate(Vector3fc amt)
	{
		localPosition.add(amt);
	}
	
	public void rotate(Quaternionfc rotation)
	{
		localRotation.mul(rotation);
	}

	/**
	 * Gets the euler angles from this transform
	 * @return The euler angles of this transform
	 */
	public Vector3fc eulers()
	{
		return rotation().getEulerAnglesXYZ(new Vector3f());
	}
	
	public Vector3fc localEulers()
	{
		return localRotation.getEulerAnglesXYZ(new Vector3f());
	}
	
	public void rotateX(float x)
	{
		new Quaternionf(new AxisAngle4f(x, axis().xEndpoint())).mul(localRotation(), localRotation);
	}
	
	public void rotateY(float y)
	{
		new Quaternionf(new AxisAngle4f(y, axis().yEndpoint())).mul(localRotation(), localRotation);
	}
	
	public void rotateZ(float z)
	{
		new Quaternionf(new AxisAngle4f(z, axis().zEndpoint())).mul(localRotation(), localRotation);
	}
	
	public float x()
	{
		return position().x();
	}

	public float y()
	{
		return position().y();
	}

	public float z()
	{
		return position().z();
	}

	public Axis axis()
	{
		return new Axis(rotation());
	}

	public float distanceSqrd(Transform transform)
	{
		return Maths.distSqrd(rotation().transform(position(), new Vector3f()), transform.rotation().transform(position(), new Vector3f()));
	}

	public Matrix4fc rotationMatrix()
	{
		return Maths.createRotationMatrix(rotation());
	}
	
	public void rotate(AxisAngle4f axisAngle)
	{
		new Quaternionf(axisAngle).mul(localRotation(), localRotation);
	}

	public Matrix4f asMatrix()
	{
		return Maths.createTransformationMatrix(position(), rotation());
	}
}
