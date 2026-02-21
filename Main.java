import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

//SINGLETON

enum LogLevel {
    INFO, WARNING, ERROR
}

class Logger {

    private static volatile Logger instance;
    private static ReentrantLock lock = new ReentrantLock();

    private LogLevel currentLevel = LogLevel.INFO;
    private String logFilePath = "app.log";
    private long maxFileSize = 1024 * 5; // 5 KB для ротации
    private boolean logToConsole = true;

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void setLogLevel(LogLevel level) {
        this.currentLevel = level;
    }

    public void setLogToConsole(boolean value) {
        this.logToConsole = value;
    }

    public void loadConfig(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts[0].equals("level")) {
                    currentLevel = LogLevel.valueOf(parts[1]);
                }
                if (parts[0].equals("file")) {
                    logFilePath = parts[1];
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка загрузки конфигурации");
        }
    }

    public void log(String message, LogLevel level) {
        if (level.ordinal() < currentLevel.ordinal()) return;

        lock.lock();
        try {
            rotateIfNeeded();

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String logMessage = time + " [" + level + "] " + message;

            FileWriter writer = new FileWriter(logFilePath, true);
            writer.write(logMessage + "\n");
            writer.close();

            if (logToConsole) {
                System.out.println(logMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private void rotateIfNeeded() {
        File file = new File(logFilePath);
        if (file.exists() && file.length() > maxFileSize) {
            File newFile = new File("app_" + System.currentTimeMillis() + ".log");
            file.renameTo(newFile);
        }
    }
}

class LogReader {

    public static void readLogs(String file, LogLevel levelFilter) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(levelFilter.toString())) {
                    System.out.println(line);
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка чтения логов");
        }
    }
}

//BUILDER

class ReportStyle {
    String backgroundColor;
    String fontColor;
    int fontSize;

    public ReportStyle(String bg, String font, int size) {
        this.backgroundColor = bg;
        this.fontColor = font;
        this.fontSize = size;
    }
}

class Report {
    String header;
    String content;
    String footer;
    ReportStyle style;
    Map<String, String> sections = new LinkedHashMap<>();

    public void export(String format) {
        System.out.println("=== Exporting " + format + " Report ===");
        System.out.println("Style: bg=" + style.backgroundColor +
                ", font=" + style.fontColor +
                ", size=" + style.fontSize);
        System.out.println(header);
        for (String key : sections.keySet()) {
            System.out.println("Section: " + key);
            System.out.println(sections.get(key));
        }
        System.out.println(content);
        System.out.println(footer);
    }

    public void exportToFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(header + "\n");
            writer.write(content + "\n");
            writer.write(footer + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

interface IReportBuilder {
    void setHeader(String header);
    void setContent(String content);
    void setFooter(String footer);
    void addSection(String name, String content);
    void setStyle(ReportStyle style);
    Report getReport();
}

class TextReportBuilder implements IReportBuilder {
    private Report report = new Report();

    public void setHeader(String header) { report.header = header; }
    public void setContent(String content) { report.content = content; }
    public void setFooter(String footer) { report.footer = footer; }
    public void addSection(String name, String content) { report.sections.put(name, content); }
    public void setStyle(ReportStyle style) { report.style = style; }
    public Report getReport() { return report; }
}

class HtmlReportBuilder implements IReportBuilder {
    private Report report = new Report();

    public void setHeader(String header) { report.header = "<h1>" + header + "</h1>"; }
    public void setContent(String content) { report.content = "<p>" + content + "</p>"; }
    public void setFooter(String footer) { report.footer = "<footer>" + footer + "</footer>"; }
    public void addSection(String name, String content) {
        report.sections.put("<h2>" + name + "</h2>", "<p>" + content + "</p>");
    }
    public void setStyle(ReportStyle style) { report.style = style; }
    public Report getReport() { return report; }
}

class PdfReportBuilder implements IReportBuilder {
    private Report report = new Report();

    public void setHeader(String header) { report.header = "PDF: " + header; }
    public void setContent(String content) { report.content = content; }
    public void setFooter(String footer) { report.footer = footer; }
    public void addSection(String name, String content) { report.sections.put(name, content); }
    public void setStyle(ReportStyle style) { report.style = style; }
    public Report getReport() { return report; }
}

class ReportDirector {
    public void construct(IReportBuilder builder, ReportStyle style) {
        builder.setStyle(style);
        builder.setHeader("Annual Report");
        builder.addSection("Sales", "Sales increased by 30%");
        builder.addSection("Marketing", "New campaign launched");
        builder.setContent("Main content here");
        builder.setFooter("End of Report");
    }
}

//PROTOTYPE

class Weapon implements Cloneable {
    String name;
    int damage;

    public Weapon(String name, int damage) {
        this.name = name;
        this.damage = damage;
    }

    protected Weapon clone() throws CloneNotSupportedException {
        return (Weapon) super.clone();
    }
}

class Armor implements Cloneable {
    String name;
    int defense;

    public Armor(String name, int defense) {
        this.name = name;
        this.defense = defense;
    }

    protected Armor clone() throws CloneNotSupportedException {
        return (Armor) super.clone();
    }
}

class Skill implements Cloneable {
    String name;
    int power;

    public Skill(String name, int power) {
        this.name = name;
        this.power = power;
    }

    protected Skill clone() throws CloneNotSupportedException {
        return (Skill) super.clone();
    }
}

class GameCharacter implements Cloneable {

    int health, strength, agility, intelligence;
    Weapon weapon;
    Armor armor;
    List<Skill> skills = new ArrayList<>();

    protected GameCharacter clone() throws CloneNotSupportedException {
        GameCharacter cloned = (GameCharacter) super.clone();
        cloned.weapon = weapon.clone();
        cloned.armor = armor.clone();
        cloned.skills = new ArrayList<>();
        for (Skill s : skills) {
            cloned.skills.add(s.clone());
        }
        return cloned;
    }

    public void show() {
        System.out.println("HP:" + health + " STR:" + strength);
        System.out.println("Weapon:" + weapon.name);
        System.out.println("Armor:" + armor.name);
        System.out.println("Skills:");
        for (Skill s : skills) {
            System.out.println("- " + s.name);
        }
    }
}

//MAIN

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("===== LOGGER TEST =====");

        Logger logger = Logger.getInstance();
        logger.setLogLevel(LogLevel.INFO);

        Runnable task = () -> {
            Logger log = Logger.getInstance();
            log.log("Info message", LogLevel.INFO);
            log.log("Warning message", LogLevel.WARNING);
            log.log("Error message", LogLevel.ERROR);
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("\nReading ERROR logs:");
        LogReader.readLogs("app.log", LogLevel.ERROR);

        System.out.println("\n===== BUILDER TEST =====");

        ReportDirector director = new ReportDirector();
        ReportStyle style = new ReportStyle("White", "Black", 14);

        IReportBuilder textBuilder = new TextReportBuilder();
        director.construct(textBuilder, style);
        textBuilder.getReport().export("TEXT");

        IReportBuilder htmlBuilder = new HtmlReportBuilder();
        director.construct(htmlBuilder, style);
        htmlBuilder.getReport().export("HTML");

        IReportBuilder pdfBuilder = new PdfReportBuilder();
        director.construct(pdfBuilder, style);
        pdfBuilder.getReport().export("PDF");

        System.out.println("\n===== PROTOTYPE TEST =====");

        GameCharacter warrior = new GameCharacter();
        warrior.health = 100;
        warrior.strength = 20;
        warrior.weapon = new Weapon("Sword", 15);
        warrior.armor = new Armor("Steel Armor", 10);
        warrior.skills.add(new Skill("Slash", 5));

        GameCharacter cloned = warrior.clone();

        System.out.println("Original:");
        warrior.show();

        System.out.println("Cloned:");
        cloned.show();
    }
}