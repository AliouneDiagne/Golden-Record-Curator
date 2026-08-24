package it.aliounediagne.epicode.goldenrecord.cli;


public class ConsoleWriter {


    public void println(String text) {
        System.out.println(text);
    }


    public void prompt(String text) {
        System.out.print(text);
    }

    public void error(String userMessage, String traceId) {
        System.out.println("  [" + traceId + "] " + userMessage);
        System.out.println("             Quote this code if you report the problem.");
    }

    public static String formatTime(int seconds) {
        boolean negative = seconds < 0;
        int abs = Math.abs(seconds);
        return (negative ? "-" : "") + abs / 60 + ":" + String.format("%02d", abs % 60);
    }


    public void banner() {
        println("");
        println("  GOLDEN RECORD CURATOR v1.0");
        println("  Interstellar message curation system");
        println("  \"To the makers of music - all worlds, all times.\"");
        println("  Type 'help' for commands, 'exit' to quit.");
        println("");
    }
}
