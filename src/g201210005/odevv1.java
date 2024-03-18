
/** 
*
* @author Tuğba dirmenci tugba.dirmenci@ogr.sakarya.edu.tr
* @since 15/04/2023
* <p>
* 2.öğretim A grubu 
* </p> 
*/


package g201210005;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class odevv1 {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Lütfen bir dosya adı belirtin.");
            System.exit(1);
        }

        String filename = args[0];

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            String line = reader.readLine();
            List<FunctionCommentStats> functionCommentStatsList = new ArrayList<>();

            while (line != null) {
                Pattern functionPattern = Pattern.compile(".*(public|private|protected)\\s+[\\w<>]+\\s+(\\w+)\\(.*\\)\\s*\\{");
                Matcher functionMatcher = functionPattern.matcher(line);

                if (functionMatcher.find()) {
                    String functionName = functionMatcher.group(2);
                    FunctionCommentStats stats = printFunctionCommentStats(reader, functionName);
                    functionCommentStatsList.add(stats);
                } else {
                    line = reader.readLine();
                }
            }

            reader.close();

            for (FunctionCommentStats stats : functionCommentStatsList) {
                System.out.println(stats);
            }

        } catch (IOException e) {
            System.err.println("Dosya okunurken bir hata oluştu: " + e.getMessage());
            System.exit(1);
        }
    }

    public static FunctionCommentStats printFunctionCommentStats(BufferedReader reader, String functionName) {
        int singleLineComments = 0;
        int multiLineComments = 0;
        int javadocComments = 0;

        try {
            BufferedWriter singleLineCommentWriter = new BufferedWriter(new FileWriter("tek satir.txt"));
            BufferedWriter multiLineCommentWriter = new BufferedWriter(new FileWriter("cok satir.txt"));
            BufferedWriter javadocCommentWriter = new BufferedWriter(new FileWriter("javadoc.txt"));

            String line = reader.readLine();

            // Tek satır yorumları ve çok satırlı yorumların başlarını bulmak için
            boolean isCommentStarted = false;
            boolean isJavadocStarted = false;
            while (line != null && !line.contains(functionName + "(")) {
                if (!isCommentStarted && !isJavadocStarted) {
                    Pattern singleLineCommentPattern = Pattern.compile(".*//.*");
                    Matcher singleLineCommentMatcher = singleLineCommentPattern.matcher(line);
                    if (singleLineCommentMatcher.matches()) {
                        singleLineComments++;
                        singleLineCommentWriter.write(line + "\n");
                        line = reader.readLine();
                        continue;
                    }

                    Pattern multiLineCommentPattern = Pattern.compile(".*\\/\\*.*");
                    Matcher multiLineCommentMatcher = multiLineCommentPattern.matcher(line);
                    if (multiLineCommentMatcher.matches()) {
                        isCommentStarted = true;
                        multiLineComments++;
                        multiLineCommentWriter.write(line + "\n");
                        line = reader.readLine();
                        continue;
                    }

                    Pattern javadocCommentPattern = Pattern.compile(".*\\/\\*\\*.*");
                    Matcher javadocCommentMatcher = javadocCommentPattern.matcher(line);
                    if (javadocCommentMatcher.matches()) {
                        isJavadocStarted = true;
                        javadocComments++;
                        javadocCommentWriter.write(line + "\n");
                        line = reader.readLine();
                        continue;
                    }
                }

                if (isCommentStarted) {
                    Pattern endMultiLineCommentPattern = Pattern.compile(".*\\*\\/.*");
                    Matcher endMultiLineCommentMatcher = endMultiLineCommentPattern.matcher(line);
                    if (endMultiLineCommentMatcher.matches()) {
                        isCommentStarted = false;
                    }
                    multiLineComments++;
                    multiLineCommentWriter.write(line + "\n");
                }

                if (isJavadocStarted) {
                    Pattern endJavadocCommentPattern = Pattern.compile(".*\\*\\/.*");
                    Matcher endJavadocCommentMatcher = endJavadocCommentPattern.matcher(line);
                    if (endJavadocCommentMatcher.matches()) {
                        isJavadocStarted = false;
                    }
                    javadocComments++;
                    javadocCommentWriter.write(line + "\n");
                }

                line = reader.readLine();
            }

            singleLineCommentWriter.close();
            multiLineCommentWriter.close();
            javadocCommentWriter.close();
        } catch (IOException e) {
            System.err.println("Dosya yazılırken bir hata oluştu: " + e.getMessage());
            System.exit(1);
        }

        return new FunctionCommentStats(functionName, singleLineComments, multiLineComments, javadocComments);
    }

    static class FunctionCommentStats {
        String functionName;
        int singleLineComments;
        int multiLineComments;
        int javadocComments;

        FunctionCommentStats(String functionName, int singleLineComments, int multiLineComments, int javadocComments) {
            this.functionName = functionName;
            this.singleLineComments = singleLineComments;
            this.multiLineComments = multiLineComments;
            this.javadocComments = javadocComments;
        }

        @Override
        public String toString() {
            return "Fonksiyon adı: " + functionName +
                    "\nTek satır yorum sayısı: " + singleLineComments +
                    "\nÇok satırlı yorum sayısı: " + multiLineComments +
                    "\nJavadoc yorum sayısı: " + javadocComments + "\n";
        }
    }
}


