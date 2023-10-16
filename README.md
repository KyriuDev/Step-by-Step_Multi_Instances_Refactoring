# Step-by-Step Refactoring
This repository contain the source code of a tool allowing the user to generate an optimized BPMN process
out of an initial process, a resource pool, and an IAT.
Each task of the BPMN process must have a name, a duration, and a set of needed resources, with the following
syntax: "name (duration) <1 resource_1, 1_resource2>".
For example, task "OpenBankAccount" could have the following name: OpenBankAccount (7) <1 employee, 1 user>.
Examples can be found in the "EXAMPLES" folder of this project.

A dependency file "<filename>.dep" must be present in the directory -- even if empty -- and must contain the
dependencies that must be preserved by the approach, in the form of tuples, one per line.
For example, if task "T1" must be executed before task "T2", the following tuple should appear in the dependency file:

(T1,T2)

A file "global_infos_current.inf" can be included in the directory to indicate the IAT, the number of instances,
and the pool of shared resources available.
If specified, this file must contain the following elements:
- On the first line of the file: "- IAT: [RealDistribution type (const, normal, exp, ...)], [first_parameter_of_the_distribution], [second_parameter_of_the_distribution]"
- On the second line of the file: "- Nb instances: [number of instances required]"
- On the third line of the file: "- Global resources: <[nb replicas of resource one] [name of resource one] ([cost of resource one]), ...>"

The main class is the class main/Main.java.
The tool can be started using the command "./gradlew main --args="working_directory"" where "working_directory"
must be replaced by the absolute path of the directory containing the process and the dependency file.
Once launched, the tool will propose textually the first task that should be moved to the user, who can accepts or decline the offer.
Once a task is accepted, its new position will be computed and the corresponding BPMN process will be written in the specified working directory.
The user can then verify that it fits his needs, and validate the step in the approach.
Otherwise, he can refuse it and a new task will be proposed for a move.
The tool finishes either after suggesting all the tasks of the process for a move, or when the user enter the keyword "quit".
