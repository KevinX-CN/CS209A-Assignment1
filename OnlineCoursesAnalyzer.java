import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower
 * version). This is just a demo, and you can extend and implement functions based on this demo, or
 * implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4],
                    info[5],
                    Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                    Integer.parseInt(info[9]), Integer.parseInt(info[10]),
                    Double.parseDouble(info[11]),
                    Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                    Double.parseDouble(info[14]),
                    Double.parseDouble(info[15]), Double.parseDouble(info[16]),
                    Double.parseDouble(info[17]),
                    Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                    Double.parseDouble(info[20]),
                    Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> map = new TreeMap<>();
        for (Course i : courses) {
            map.merge(i.getInstitution(), i.getParticipants(), Integer::sum);
        }
        return map;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> map = new TreeMap<>();
        for (Course i : courses) {
            map.merge(i.getInstitutionAndSubject(), i.getParticipants(), Integer::sum);
        }
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });
        map = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> mapping : list) {
            map.put(mapping.getKey(), mapping.getValue());
        }
        return map;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> map = new LinkedHashMap<>();
        for (Course i : courses) {
            if (i.getInstructor().indexOf(',') == -1) {
                List<List<String>> list = new ArrayList<List<String>>();
                List<String> listList = new ArrayList<String>();
                listList.add(i.getCourseTitle());
                list.add(listList);
                listList = new ArrayList<String>();
                list.add(listList);
                map.merge(i.getInstructor(), list, (o, n) -> {
                    if (o.get(0).indexOf(i.getCourseTitle()) == -1) {
                        List<List<String>> l = o;
                        l.get(0).add(i.getCourseTitle());
                        return l;
                    }
                    return o;
                });
            } else {
                String[] instructors = i.getInstructor().split(",");
                for (String j : instructors) {
                    List<List<String>> list = new ArrayList<List<String>>();
                    List<String> listList = new ArrayList<String>();
                    list.add(listList);
                    listList = new ArrayList<String>();
                    listList.add(i.getCourseTitle());
                    list.add(listList);
                    map.merge(j, list, (o, n) -> {
                        if (o.get(0).indexOf(i.getCourseTitle()) == -1) {
                            List<List<String>> l = o;
                            l.get(1).add(i.getCourseTitle());
                            return l;
                        }
                        return o;
                    });
                }
            }
        }
        return map;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        if (by == "hours") {
            return courses.stream().sorted((p, q) -> {
                if (p.getHours() > q.getHours()) {
                    return -1;
                } else if (p.getHours() == q.getHours()) {
                    return 0;
                } else {
                    return 1;
                }
            }).map(Course::getCourseTitle).distinct().limit(topK).toList();
        }
        if (by == "participants") {
            return courses.stream().sorted((p, q) -> {
                if (p.getParticipants() > q.getParticipants()) {
                    return -1;
                } else if (p.getParticipants() == q.getParticipants()) {
                    return 0;
                } else {
                    return 1;
                }
            }).map(Course::getCourseTitle).distinct().limit(topK).toList();
        }
        return null;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited,
        double totalCourseHours) {
        return courses.stream().filter((p) -> {
            return p.getSubject().toLowerCase().contains(courseSubject.toLowerCase())
                && p.getPercentAudited() >= percentAudited && p.getHours() <= totalCourseHours;
        }).map(Course::getCourseTitle).distinct().sorted().toList();
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        return courses.stream().sorted((p, q) -> {
            double MarkP =
                (age - p.getAge()) * (age - p.getAge()) + (gender - p.getPercentMale()) * (
                    gender - p.getPercentMale()) + (isBachelorOrHigher - p.getPercentDegree()) * (
                    isBachelorOrHigher - p.getPercentDegree());
            double MarkQ =
                (age - q.getAge()) * (age - q.getAge()) + (gender - q.getPercentMale()) * (
                    gender - q.getPercentMale()) + (isBachelorOrHigher - q.getPercentDegree()) * (
                    isBachelorOrHigher - q.getPercentDegree());
            if (MarkP > MarkQ) {
                return 1;
            } else if (MarkP == MarkQ) {
                return 0;
            } else {
                return -1;
            }
        }).limit(10).map(Course::getCourseTitle).toList();
    }

    class Course {

        String institution;
        String number;
        Date launchDate;
        String title;
        String instructors;
        String subject;
        int year;
        int honorCode;
        int participants;
        int audited;
        int certified;
        double percentAudited;
        double percentCertified;
        double percentCertified50;
        double percentVideo;
        double percentForum;
        double gradeHigherZero;
        double totalHours;
        double medianHoursCertification;
        double medianAge;
        double percentMale;
        double percentFemale;
        double percentDegree;

        public Course(String institution, String number, Date launchDate,
            String title, String instructors, String subject,
            int year, int honorCode, int participants,
            int audited, int certified, double percentAudited,
            double percentCertified, double percentCertified50,
            double percentVideo, double percentForum, double gradeHigherZero,
            double totalHours, double medianHoursCertification,
            double medianAge, double percentMale, double percentFemale,
            double percentDegree) {
            this.institution = institution;
            this.number = number;
            this.launchDate = launchDate;
            if (title.startsWith("\"")) {
                title = title.substring(1);
            }
            if (title.endsWith("\"")) {
                title = title.substring(0, title.length() - 1);
            }
            this.title = title;
            if (instructors.startsWith("\"")) {
                instructors = instructors.substring(1);
            }
            if (instructors.endsWith("\"")) {
                instructors = instructors.substring(0, instructors.length() - 1);
            }
            this.instructors = instructors;
            if (subject.startsWith("\"")) {
                subject = subject.substring(1);
            }
            if (subject.endsWith("\"")) {
                subject = subject.substring(0, subject.length() - 1);
            }
            this.subject = subject;
            this.year = year;
            this.honorCode = honorCode;
            this.participants = participants;
            this.audited = audited;
            this.certified = certified;
            this.percentAudited = percentAudited;
            this.percentCertified = percentCertified;
            this.percentCertified50 = percentCertified50;
            this.percentVideo = percentVideo;
            this.percentForum = percentForum;
            this.gradeHigherZero = gradeHigherZero;
            this.totalHours = totalHours;
            this.medianHoursCertification = medianHoursCertification;
            this.medianAge = medianAge;
            this.percentMale = percentMale;
            this.percentFemale = percentFemale;
            this.percentDegree = percentDegree;
        }

        public String getInstitution() {
            return this.institution;
        }

        public String getInstitutionAndSubject() {
            return this.institution + "-" + this.subject;
        }

        public String getInstructor() {
            return this.instructors;
        }

        public double getHours() {
            return this.totalHours;
        }

        public int getParticipants() {
            return this.participants;
        }

        public String getCourseTitle() {
            return this.title;
        }

        public double getPercentAudited() {
            return this.percentAudited;
        }

        public String getSubject() {
            return this.subject;
        }

        public double getAge() {
            return this.medianAge;
        }

        public double getPercentMale() {
            return this.percentMale;
        }

        public double getPercentDegree() {
            return this.percentDegree;
        }
    }
}