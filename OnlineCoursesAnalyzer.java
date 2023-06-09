import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
        Map<String, List<Set<String>>> map = new HashMap<>();
        for (Course i : courses) {
            if (i.getInstructor().indexOf(',') == -1) {
                List<Set<String>> list = new ArrayList<Set<String>>();
                Set<String> setList = new TreeSet<String>();
                setList.add(i.getCourseTitle());
                list.add(setList);
                list.add(new TreeSet<String>());
                map.merge(i.getInstructor(), list, (o, n) -> {
                    List<Set<String>> l = o;
                    l.get(0).add(i.getCourseTitle());
                    return l;
                });
            } else {
                String[] instructors = i.getInstructor().split(", ");
                for (String j : instructors) {
                    List<Set<String>> list = new ArrayList<Set<String>>();
                    list.add(new TreeSet<String>());
                    Set<String> setList = new TreeSet<String>();
                    setList.add(i.getCourseTitle());
                    list.add(setList);
                    map.merge(j, list, (o, n) -> {
                        List<Set<String>> l = o;
                        l.get(1).add(i.getCourseTitle());
                        return l;
                    });
                }
            }
        }
        List<Entry<String, List<Set<String>>>> mapList = new ArrayList<Entry<String, List<Set<String>>>>(
            map.entrySet());
        Map<String, List<List<String>>> map1 = new HashMap<>();
        for (Map.Entry<String, List<Set<String>>> i : mapList) {
            List<List<String>> list = new ArrayList<>();
            list.add(i.getValue().get(0).stream().toList());
            list.add(i.getValue().get(1).stream().toList());
            map1.put(i.getKey(), list);
        }
        return map1;
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
        Map<String, List<Double>> map = new HashMap<>();
        for (Course i : courses) {
            map.merge(i.getCourseNumber(), i.getNumbers(), (o, n) ->
            {
                List<Double> l = new ArrayList<>();
                l.add((o.get(0) * o.get(3) + n.get(0) * n.get(3)) / (o.get(3) + n.get(3)));
                l.add((o.get(1) * o.get(3) + n.get(1) * n.get(3)) / (o.get(3) + n.get(3)));
                l.add((o.get(2) * o.get(3) + n.get(2) * n.get(3)) / (o.get(3) + n.get(3)));
                l.add(o.get(3) + 1);
                return l;
            });
        }
        Map<String, Optional<Course>> numberToTitle = courses.stream().collect(
            Collectors.groupingBy(Course::getCourseNumber,
                Collectors.maxBy(Comparator.comparing(Course::getLaunchDate))));

        return courses.stream().sorted((p, q) -> {
            double MarkP =
                (age - map.get(p.getCourseNumber()).get(0)) * (age - map.get(p.getCourseNumber())
                    .get(0)) + (gender * 100 - map.get(p.getCourseNumber()).get(1)) * (gender * 100
                    - map.get(p.getCourseNumber()).get(1))
                    + (isBachelorOrHigher * 100 - map.get(p.getCourseNumber()).get(2)) * (
                    isBachelorOrHigher * 100 - map.get(p.getCourseNumber()).get(2));
            String TitleP=numberToTitle.get(p.getCourseNumber()).get().getCourseTitle();
            double MarkQ =
                (age - map.get(q.getCourseNumber()).get(0)) * (age - map.get(q.getCourseNumber())
                    .get(0)) + (gender * 100 - map.get(q.getCourseNumber()).get(1)) * (gender * 100
                    - map.get(q.getCourseNumber()).get(1))
                    + (isBachelorOrHigher * 100 - map.get(q.getCourseNumber()).get(2)) * (
                    isBachelorOrHigher * 100 - map.get(q.getCourseNumber()).get(2));
            String TitleQ=numberToTitle.get(q.getCourseNumber()).get().getCourseTitle();
            if (MarkP > MarkQ) {
                return 1;
            } else if (MarkP == MarkQ) {
                return TitleP.compareTo(TitleQ);
            } else {
                return -1;
            }
        }).map((e) ->
        {
            return e.getCourseNumber();
        }).distinct().map((e) ->
        {
            return numberToTitle.get(e).get().getCourseTitle();
        }).distinct().limit(10).toList();
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

        public String getCourseNumber() {
            return this.number;
        }

        public Date getLaunchDate() {
            return this.launchDate;
        }

        public List<Double> getNumbers() {
            List<Double> l = new ArrayList<>();
            l.add(this.medianAge);
            l.add(this.percentMale);
            l.add(this.percentDegree);
            l.add(1.0);
            return l;
        }
    }
}