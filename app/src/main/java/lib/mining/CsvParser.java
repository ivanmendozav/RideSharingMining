package lib.mining;

import android.os.Environment;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Ivan on 05/05/2015.
 */
public class CsvParser {
    public static List<String> ParseFile(String filename){
        List<String> lines = new ArrayList<>();
        File fFilePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/"+filename);

        try {
            Scanner scanner = new Scanner(fFilePath);
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            return lines;
        }catch(Exception e){
            return null;
        }
    }
}
