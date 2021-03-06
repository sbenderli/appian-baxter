package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.CommandResult;
import com.appian.appianbaxter.util.TimeoutInputStream;
import com.google.common.primitives.Ints;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An object that manages IO to/from baxter
 *
 * @author serdar
 */
public class BaxterIO {

    private static final boolean USE_TIMEOUT_READ = true;
    private final ProcessBuilder pb;

    private final static int READ_BUFFER = 1024;
    private final static int DEFAULT_READ_TIMEOUT = 10000;
    private final static int READ_CLOSE_TIMEOUT = 10000;

    private Map<Integer, Process> processMap = new HashMap<>();
    private Map<Integer, Command> commandMap = new HashMap<>();
    private Map<Integer, BufferedReader> readerMap = new HashMap<>();
    private Map<Integer, BufferedWriter> writerMap = new HashMap<>();

    private Object lock = new Object();

    public static String START_FOLDER;

    public BaxterIO(ProcessBuilder pb) {
        this.pb = pb;
    }

    public String getFolderPath() {
        return pb.directory().getAbsolutePath();
    }

    public Process getNewProcess(int timeout) {
        Process process;
        try {
            process = pb.start();
            Integer pid = getProcessPid(process);
            processMap.put(pid, process);
            readerMap.put(pid, new BufferedReader(
                    new InputStreamReader(
                            timeout == -1
                                    ? process.getInputStream()
                                    : new TimeoutInputStream(
                                            process.getInputStream(),
                                            READ_BUFFER, timeout * 1000,//to ms
                                            READ_CLOSE_TIMEOUT))));
            writerMap.put(pid, new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream())));
            return process;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to start the process. Exception: " + ex.getMessage());
        }
    }

    public CommandResult sendCommand(Command command) {
        //lastSentCommand = command;
        if (command == null || command.getCommand() == null
                || command.getCommand().isEmpty()) {
            return new CommandResult(null, null, null);
        }
        Process process = getNewProcess(command.getReadTimeout());
        write(command.getCommand(), process);

        CommandResult result = new CommandResult();
        result.setPid(getProcessPid(process));
        result.setSentCommand(command);
        if (command.isWaitForResult()) {
            result.setMessage(read(process));
            //Kick-off kill process
            Thread t = new Thread(
                    () -> {
                        this.killProcessAndItsChildren(process);
                    });
            t.start();
        } else {
            commandMap.put(result.getPid(), command);
            result.setMessage("You are doing an async operation. "
                    + "Call read/{pid} endpoint to get the result of "
                    + "your command.");
        }
        result.setSuccess(true);
        return result;
    }

    public Command getLastSentCommand(Integer pid) {
        return commandMap.get(pid);
    }

    public CommandResult readResult(Integer pid) {
        CommandResult result = new CommandResult();
        Process process = processMap.get(pid);
        result.setPid(pid);
        result.setSentCommand(commandMap.get(pid));
        result.setMessage(read(process));
        return result;
    }

    public String killProcessAndItsChildren(Integer pid) {
        return killProcessAndItsChildren(processMap.get(pid));
    }

    public String killProcessAndItsChildren(Process process) {
        if (process == null) return "";
        Integer processPid = getProcessPid(process);
        System.out.println("Starting process cleanup for " + processPid);

        StringBuilder sb = new StringBuilder(
                "Killed following child processes: ");
        List<Integer> subProcesPids = new ArrayList<>();

        //Start a new process to get sub pids and kill them
        //We can't do this from main process because it might be
        //running a process currently
        Process tempProcess = getNewProcess(DEFAULT_READ_TIMEOUT);
        //1234
        //5678
        //7890
        String[] tokens = writeAndRead(
                String.format("ps --ppid %s | awk '{ print $1 }'", processPid),
                tempProcess).split(System.getProperty("line.separator"));
        for (String token : tokens) {
            Integer temp = Ints.tryParse(token);
            if (temp != null) {
                subProcesPids.add(temp);
            }
        }

        //Kill each process
        for (Integer pid : subProcesPids) {
//            String pidName = writeAndRead(
//                    String.format("ps -p %s -o command=", pid), tempProcess)
//                    .trim();
//            sb.append(System.getProperty("line.separator"))
//                    .append(String.format("%s (%s)", pidName, pid));
            sb.append(String.format("%s,", pid));
            writeAndRead(String.format("kill %s && echo done", pid),
                    tempProcess);
        }
        //destroy the temp process
        destroyProcess(tempProcess);
        destroyProcess(process);
        sb.append(System.getProperty("line.separator"))
                .append(String.format("Successfully killed "
                        + "parent process: (%s)", processPid));

        String result = sb.toString();
        System.out.println(result);
        System.out.println("Finished process cleanup for " + processPid);

        return result;
    }

    public String killAllProcesses() {
        StringBuilder sb = new StringBuilder(
                "Started killing all processes...");
        
        List<Process> processes = new ArrayList<>(processMap.values());
        for (Process p : processes) {
            sb.append(killProcessAndItsChildren(p));
        }
        return sb.toString();
    }

    //<editor-fold defaultstate="collapsed" desc="Private methods">
    private void destroyProcess(Process process) {
        if (process == null) return;
        Integer pid = getProcessPid(process);

        synchronized (lock) {
            readerMap.remove(pid);
            writerMap.remove(pid);
            processMap.remove(pid);
            commandMap.remove(pid);
        }
        process.destroy();
    }

    private String writeAndRead(String command, Process process) {
        write(command, process);
        return read(process);
    }

    private void write(String command, Process process) {
        try {
            BufferedWriter bufferedWriter = writerMap.get(getProcessPid(process));
            bufferedWriter.write(command + System.getProperty("line.separator"));
            bufferedWriter.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write to process");
        }
    }

    private String read(Process process) {
        String line = "";
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = readerMap.get(getProcessPid(process));
        try {
            //Read buffer is not always immediately ready after a write
            //Wait a modest amount before checking for the buffer
            //Thread.sleep(1000);
            do {
                line = bufferedReader.readLine();
                sb.append(System.getProperty("line.separator")).append(line);
                System.out.println("Stdout: " + line);
            } while (bufferedReader.ready());
        } catch (InterruptedIOException e) {
            //Do something
            sb.append("---Read timed out---");
        } catch (IOException e) {
            sb.append("IOException occurred: ").append(e.getMessage());
        }
        return sb.toString();
    }

    private static int getProcessPid(Process p) {
        int pid = -1;

        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getInt(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }
//</editor-fold>

}
