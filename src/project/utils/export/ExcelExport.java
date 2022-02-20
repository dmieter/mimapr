
package project.utils.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import org.apache.poi.hssf.usermodel.HSSFCellStyle;
//import org.apache.poi.hssf.usermodel.HSSFFont;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author emelyanov
 */
public class ExcelExport {
    
    public static boolean exportToExcel(String path, ExcelExportable obj) throws FileNotFoundException, IOException{
        
//        HSSFWorkbook workbook = new HSSFWorkbook();
//        HSSFSheet sheet = workbook.createSheet("Employees sheet");
//
//
//        Cell cell;
//        Row row;
//        //
//        HSSFCellStyle style = createStyleForTitle(workbook);
//
//        row = sheet.createRow(0);
//
//        // EmpNo
//        cell = row.createCell(0, CellType.STRING);
//        cell.setCellValue("EmpNo");
//        cell.setCellStyle(style);
//        // EmpName
//        cell = row.createCell(1, CellType.STRING);
//        cell.setCellValue("EmpNo");
//        cell.setCellStyle(style);
//        // Salary
//        cell = row.createCell(2, CellType.STRING);
//        cell.setCellValue("Salary");
//        cell.setCellStyle(style);
//        // Grade
//        cell = row.createCell(3, CellType.STRING);
//        cell.setCellValue("Grade");
//        cell.setCellStyle(style);
//        // Bonus
//        cell = row.createCell(4, CellType.STRING);
//        cell.setCellValue("Bonus");
//        cell.setCellStyle(style);
//
//        // Data
//        for (int i = 1; i< 10; i++) {
//
//            row = sheet.createRow(i);
//
//            // EmpNo (A)
//            cell = row.createCell(0, CellType.STRING);
//            cell.setCellValue(1);
//            // EmpName (B)
//            cell = row.createCell(1, CellType.STRING);
//            cell.setCellValue("dieter");
//            // Salary (C)
//            cell = row.createCell(2, CellType.NUMERIC);
//            cell.setCellValue("10");
//            // Grade (D)
//            cell = row.createCell(3, CellType.NUMERIC);
//            cell.setCellValue(5);
//            // Bonus (E)
//            String formula = "0.1*C" + (i + 1) + "*D" + (i + 1);
//            cell = row.createCell(4, CellType.FORMULA);
//            cell.setCellFormula(formula);
//        }
//        File file = new File(path);
//        file.getParentFile().mkdirs();
//
//        FileOutputStream outFile = new FileOutputStream(file);
//        workbook.write(outFile);
//        System.out.println("Created file: " + file.getAbsolutePath());
        
        return false;
    }
    
//    private static HSSFCellStyle createStyleForTitle(HSSFWorkbook workbook) {
//        HSSFFont font = workbook.createFont();
//        font.setBold(true);
//        HSSFCellStyle style = workbook.createCellStyle();
//        style.setFont(font);
//        return style;
//    }
}
