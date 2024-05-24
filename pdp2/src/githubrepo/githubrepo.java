
/**
*
* Tugba Dirmenci 
* Tugba.dirmenci@ogr.sakarya.edu.tr
* 07/04/2024
* githubrepo
*/

	package githubrepo;

	import java.io.BufferedReader;
	import java.io.File;
	import java.io.FileReader;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.util.ArrayList;
	import java.util.List;

	public class githubrepo {
	    public static void main(String[] args) {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); //dosya içerigi satır satır okunur.
	
	        try {
	            System.out.print("GitHub Repository URL: "); //kullanıcıdan url alır.
	            String repoUrl = reader.readLine();
	
	            // Depoyu klonla
	            String command = "git clone " + repoUrl; //klonlama işlemi için gerekli komutu içerir.
	            Process process = Runtime.getRuntime().exec(command); //belirlenen komutun çalıstırılması için process olusturur.
	            process.waitFor(); //git clone komutunun tamamlanmasını bekler.
	
	            File clonedRepo = new File(getRepoNameFromUrl(repoUrl)); //depo klonlanır
	
	            if (!clonedRepo.exists()) {
	                System.out.println("Klonlanan depo bulunamadı."); //klonlanamadı uyarı verir.
	                return;
	            }
	
	            List<File> javaFiles = findJavaFiles(clonedRepo); //java dosyalarını içeren bir liste olusturur.
	
	            if (javaFiles.isEmpty()) {
	                System.out.println("Klonlanan depoda *.java uzantılı dosya bulunamadı.");
	                return;
	            }
	
	            for (File javaFile : javaFiles) { //her bir java dosyası analyzeJavaFile metoduna gonderilir.
	                analyzeJavaFile(javaFile);
	            }
	
	        	} catch (IOException | InterruptedException e) { //Eğer dosya okuma veya dosya açma işlemleri bir hata oluşturursa,  
	            e.printStackTrace();						//bu hata catch bloğunda ele alınır ve hata detayları yazdırılır
	        }
    }

    private static String getRepoNameFromUrl(String url) {   
        String[] parts = url.split("/");					 //github depo url'sinden git adını çıkarır 
        String repoName = parts[parts.length - 1];			//ve sadece depo adını string olarak alır.
        return repoName.replace(".git", "");   	 
    }

    private static List<File> findJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();

        findJavaFilesRecursive(directory, javaFiles); //bir dizin içerisindeki tüm java dosyalarını bulur.

        return javaFiles;
    }

    private static void findJavaFilesRecursive(File directory, List<File> javaFiles) {
        File[] files = directory.listFiles(); // Verilen dizin içindeki tüm dosyaları bir dizi olarak alır.

        if (files != null) {    //Dosya dizisi null değilse (yoksa işlem yapılmaz), dosya dizisi üzerinde döngü oluşturulur
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFilesRecursive(file, javaFiles); //findJavaFilesRecursive metodunu özyinelemeli olarak çağırır.
                } else if (file.getName().endsWith(".java")) {
                	  if (containsClass(file)) { //bu dosyanın bir sınıf içerip içermediğini kontrol etmek için çağrılır.
                	javaFiles.add(file); //eğer dosya sınıf içeriyorsa JavaFiles eklenir.
                }
            }
        }
    }
 }
    private static boolean containsClass(File javaFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) { //dosya içeriğini satır satır okur.
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(" class ") && line.endsWith("{")) {  //class olup olmadıgını kontrol eder.
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); //hata kontrolu yapılır.
        }
        return false;
    }

    private static void analyzeJavaFile(File javaFile) {
        System.out.println("Sınıf: " + javaFile.getName()); //analiz edilen java dosyası yazılır.

        int javadocLines = 0; //javadoc satır sayısı 
        int otherComments = 0; //yorum satır sayısı
        int codeLines = 0; //kod satır sayısı
        int totalLines = 0; //LOC 
        int functionCount = 0; //fonksiyon sayısı

        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
            String line;
            boolean inBlockComment = false; //block içi kontrolu

            while ((line = reader.readLine()) != null) {
                totalLines++;

                line = line.trim(); //boşluklar olabilir . Bu yüzden kullandım.

                if (line.startsWith("//")) {   // "//" ile başlıyorsa yorum satırı sayısı arttırır.
                    otherComments++;
                } else if (line.startsWith("/**")) { // "/**" ile başlıyorsa javadoc sayısı arttırır.
                    javadocLines++;
                    if (!line.endsWith("*/")) {
                        while ((line = reader.readLine()) != null) { //dosyanın sonuna kadar okuma yapar.
                            totalLines++; //loc arttırır.
                            javadocLines++;   
                            if (line.trim().endsWith("*/")) {
                                break;
                            }
                        }
                    }
                } else if (line.startsWith("/*")) { 
                    otherComments++;
                    inBlockComment = true;  //çok satırlı yorum blogu içinde olup olmadıgını kontrol eder.
                    if (!line.endsWith("*/")) { //eger satır sonu böyle degilse while donugusune girer.
                        while ((line = reader.readLine()) != null) {//dosyanın sonuna kadar okuma yapar.
                            totalLines++;
                            otherComments++;
                            if (line.trim().endsWith("*/")) {
                                inBlockComment = false; //cok satırlı yorum blogunun sonlandıgını gosterir.
                                break;
                            }
                        }
                    }
                } else if (line.endsWith("*/")) { // "*/" ile bitip bitmedigini kontrol eder.
                    inBlockComment = false; 
                    otherComments++; 				//toplam yorum satırı tutar
                } else if (!line.isEmpty() && !inBlockComment) {  //boş olup olmadıgını cok satırlı yorum blogu icinde olup olmadıgı 
                    codeLines++;   							// eger  bos degilse ve yorum blogu icinde degilse kod satırıdır.
                }

                if (line.contains("{") && line.contains("(") && line.contains(")")) {
                    functionCount++; 					//eger burdaki karakterleri içeriyorsa fonksiyon tanımı olabilir. 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Javadoc Satır Sayısı: " + javadocLines); //elde edilen analiz sonucları ekrana yazdırılır.
        System.out.println("Yorum Satır Sayısı: " + otherComments);
        System.out.println("Kod Satır Sayısı: " + codeLines);
        System.out.println("LOC: " + totalLines);
        System.out.println("Fonksiyon Sayısı: " + functionCount);

        int expectedComments = javadocLines + otherComments;
        double yg = ((double) expectedComments * 0.8) / functionCount;  //dokumanda verilen formul degerleridir.
        double yh = ((double) codeLines / functionCount) * 0.3;
        double commentDeviationPercentage = ((100 * yg) / yh) - 100; //yorum sapma yüzdesi ifadesidir.

        System.out.printf("Yorum Sapma Yüzdesi: %.2f%%%n", commentDeviationPercentage);
        System.out.println("-------------------------");
    }

}
