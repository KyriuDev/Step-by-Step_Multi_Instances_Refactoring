package simulator.bpmn_to_model_input;

import java.io.File;
import java.io.FileNotFoundException;

public abstract class Manager
{
	public abstract void write(final File workingDirectory) throws FileNotFoundException;
}
