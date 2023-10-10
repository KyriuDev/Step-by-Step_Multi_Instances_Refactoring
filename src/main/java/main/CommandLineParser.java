package main;

import other.Utils;
import constants.CommandLineOption;
import refactoring.legacy.dependencies.DependenciesParser;
import resources.ResourcePool;
import resources.ResourcePoolParser;
import simulator.bpmn_to_model_input.GlobalInfosParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommandLineParser
{
    private final Map<CommandLineOption, Object> commands;

    public CommandLineParser(final String[] args) throws FileNotFoundException
    {
        this.commands = new HashMap<>();
        this.parse(args);

        if (this.commands.containsKey(CommandLineOption.HELP))
        {
            System.out.println(this.helpMessage());
            this.commands.clear();
        }

        this.retrieveArgsFromWorkingDirectory();

        if (!this.verifyArgs())
        {
            throw new IllegalStateException("Necessary arguments are missing. Please make sure you have specified the" +
                    "following elements: IAT, BPMN process, resource pool, or that the working directory that you have" +
                    "specified contains all of these elements.");
        }
    }

    public Object get(CommandLineOption commandLineOption)
    {
        return this.commands.get(commandLineOption);
    }

    public boolean containsKey(final CommandLineOption key)
    {
        return this.commands.containsKey(key);
    }

    //Private methods

    private String helpMessage()
    {
        //TODO
        return "";
    }

    private void parse(String[] commandLineArgs)
    {
        for (String arg : commandLineArgs)
        {
            if (this.isWorkingDirectory(arg))
            {
                this.commands.put(CommandLineOption.WORKING_DIRECTORY, new File(arg));
            }
            else if (this.isOverwrite(arg))
            {
                this.commands.put(CommandLineOption.OVERWRITE, true);
            }
            else
            {
                throw new IllegalStateException("Unrecognized argument |" + arg + "|.");
            }
        }
    }

    private boolean isIAT(final String arg)
    {
        return Utils.isAnInt(arg);
    }

    private boolean isResourcePool(final String arg)
    {
        return (arg.endsWith(".rsp") || arg.endsWith(".rp"))
                && new File(arg).isFile();
    }

    private boolean isGlobalInfo(final String arg)
    {
        return arg.endsWith(".inf")
                && new File(arg).isFile();
    }

    private boolean isBpmnProcess(final String arg)
    {
        return arg.endsWith(".bpmn")
                && new File(arg).isFile();
    }

    private boolean isWorkingDirectory(final String arg)
    {
        return new File(arg).isDirectory();
    }

    private boolean isDependencies(final String arg)
    {
        return arg.endsWith(".dep")
                && new File(arg).isFile();
    }

    private boolean isOverwrite(final String arg)
    {
        final String lowerArg = arg.toLowerCase();

        return lowerArg.equals("-f")
                || lowerArg.equals("--f")
                || lowerArg.equals("-overwrite")
                || lowerArg.equals("--overwrite");
    }

    private void retrieveArgsFromWorkingDirectory() throws FileNotFoundException
    {
        if (this.commands.get(CommandLineOption.WORKING_DIRECTORY) == null) return;

        //Working directory was specified --> check whether it contains a BPMN process and/or a resource file
        final File workingDirectory = (File) this.commands.get(CommandLineOption.WORKING_DIRECTORY);

        for (File file : Objects.requireNonNull(workingDirectory.listFiles()))
        {
            if (this.isBpmnProcess(file.getPath()))
            {
                if (this.commands.containsKey(CommandLineOption.BPMN_PROCESS))
                {
                    System.out.println("Warning: BPMN Process |" +
                            ((File) this.commands.get(CommandLineOption.BPMN_PROCESS)).getPath() + "| will be" +
                            " overwritten by BPMN process |" + file.getPath() + "|.");
                }

                this.commands.put(CommandLineOption.BPMN_PROCESS, file);
            }
            else if (this.isResourcePool(file.getPath()))
            {
                final ResourcePool resourcePool = ResourcePoolParser.parse(file);

                if (this.commands.containsKey(CommandLineOption.REAL_POOL))
                {
                    System.out.println("Warning: Resource pool |" +
                            this.commands.get(CommandLineOption.REAL_POOL).toString() + "| will be" +
                            " overwritten by Resource pool |" + resourcePool + "|.");
                }

                this.commands.put(CommandLineOption.REAL_POOL, resourcePool);
            }
            else if (this.isGlobalInfo(file.getPath()))
            {
                final GlobalInfosParser parser = new GlobalInfosParser(file);

                if (this.commands.containsKey(CommandLineOption.GLOBAL_INFORMATION))
                {
                    System.out.println("Warning: Global information |" +
                            this.commands.get(CommandLineOption.GLOBAL_INFORMATION).toString() + "| will be" +
                            " overwritten by global information |" + file.getPath() + "|.");
                }

                this.commands.put(CommandLineOption.GLOBAL_INFORMATION, parser.parse());
            }
            else if (this.isDependencies(file.getPath()))
            {
                final DependenciesParser parser = new DependenciesParser(file);

                if (this.commands.containsKey(CommandLineOption.GLOBAL_DEPENDENCIES))
                {
                    System.out.println("Warning: Global dependencies |" +
                            this.commands.get(CommandLineOption.GLOBAL_DEPENDENCIES).toString() + "| will be" +
                            " overwritten by global dependencies |" + file.getPath() + "|.");
                }

                this.commands.put(CommandLineOption.GLOBAL_DEPENDENCIES, parser.parse());
            }
        }
    }

    private boolean verifyArgs()
    {
        return this.commands.get(CommandLineOption.WORKING_DIRECTORY) != null
                && this.commands.get(CommandLineOption.BPMN_PROCESS) != null
                && this.commands.get(CommandLineOption.GLOBAL_DEPENDENCIES) != null;
    }
}
