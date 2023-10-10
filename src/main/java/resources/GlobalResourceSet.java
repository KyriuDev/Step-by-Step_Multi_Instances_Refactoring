package resources;

import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.Task;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class GlobalResourceSet
{
	private final LinkedHashSet<Resource> resourcesSet;
	private final ArrayList<BpmnProcessObject> objects;
	private final ResourcePool resourcePool;

	public GlobalResourceSet(final ArrayList<BpmnProcessObject> objects)
	{
		this.objects = objects;
		this.resourcesSet = new LinkedHashSet<>();
		this.resourcePool = null;
	}

	public GlobalResourceSet(final ResourcePool pool)
	{
		this.objects = null;
		this.resourcesSet = new LinkedHashSet<>();
		this.resourcePool = pool;
	}

	public void computeGlobalResources()
	{
		if (this.objects != null)
		{
			for (BpmnProcessObject object : objects)
			{
				if (object instanceof Task)
				{
					resourcesSet.addAll(((Task) object).resourceUsage().resources());
				}
			}
		}
		else
		{
			this.resourcesSet.addAll(this.resourcePool.resources());
		}
	}

	public LinkedHashSet<Resource> resourcesSet()
	{
		return this.resourcesSet;
	}
}
