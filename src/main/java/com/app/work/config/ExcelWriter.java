package com.app.work.config;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    private final Workbook workbook;
    private final Map<String, Object> data;
    private final HttpServletResponse response;

    /* 생성자 */
    public ExcelWriter(Workbook workbook, Map<String, Object> data, HttpServletResponse response) {
        this.workbook = workbook;
        this.data = data;
        this.response = response;
    }

    /* 엑셀 파일 생성 */
    public void create() {
        setFileName(response, mapToFileName());
        Sheet sheet = workbook.createSheet();
        createHead(sheet, mapToHeadList());
        createBody(sheet, mapToBodyList());
    }

    /* 모델 객체에서 파일 이름 꺼내기 */
    private String mapToFileName() {
        return (String) data.get("filename");
    }

    /* 모델 객체에서 헤더 이름 리스트 꺼내기 */
    @SuppressWarnings("unchecked")
    private List<String> mapToHeadList() {
        return (List<String>) data.get("head");
    }

    /* 모델 객체에서 바디 데이터 리스트 꺼내기 */
    @SuppressWarnings("unchecked")
    private List<List<String>> mapToBodyList() {
        return (List<List<String>>) data.get("body");
    }

    /* 파일 이름 지정 */
    private void setFileName(HttpServletResponse response, String fileName) {
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + getFileExtension(fileName) + "\"");
    }

    /* 넘어온 뷰에 따라서 확장자 결정 */
    private String getFileExtension(String fileName) {
        if (workbook instanceof XSSFWorkbook) {
            fileName += ".xlsx";
        }
        if (workbook instanceof SXSSFWorkbook) {
            fileName += ".xlsx";
        }
        if (workbook instanceof HSSFWorkbook) {
            fileName += ".xls";
        }

        return fileName;
    }

    /* 엑셀 헤더 생성 */
    private void createHead(Sheet sheet, List<String> headList) {
        createHeadRow(sheet, headList, 0);
    }

    /* 엑셀 바디 생성 */
    private void createBody(Sheet sheet, List<List<String>> bodyList) {
        int rowSize = bodyList.size();
        for (int i = 0; i < rowSize; i++) {
            createBodyRow(sheet, bodyList.get(i), i + 1);
//        	createRow(sheet, bodyList.get(i), i + 1);
        }
    }

    /* 행 생성(Body : 넓이 자동 조절있음, 생성시간 기~임) */
    @SuppressWarnings("unused")
    private void createRow(Sheet sheet, List<String> cellList, int rowNum) {
        int size = cellList.size();
        Row row = sheet.createRow(rowNum);
        // 스타일 생성
        CellStyle style = workbook.createCellStyle();
        // 테두리 설정
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 첫 번째 행 가져오기
        Row firstRow = sheet.getRow(0);
        for (int i = 0; i < size; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(cellList.get(i));
            // 셀에 스타일 적용
            cell.setCellStyle(style);
            // 첫 번째 행의 셀 가져오기
            Cell cellOfFirstRow = firstRow.getCell(i);
            // 첫 번째 행의 셀의 너비 가져오기 (단위: 1/256)
            int cellWidthOfFirstRow = sheet.getColumnWidth(cellOfFirstRow.getColumnIndex());

            // 모든 열에 대해 셀의 너비 자동 조정
            sheet.autoSizeColumn(i);
            // 모든 열에 대해 추가 너비 설정
            sheet.setColumnWidth(i, sheet.getColumnWidth(i));

            // 셀의 너비 가져오기 (단위: 1/256)
            int cellWidth = sheet.getColumnWidth(cell.getColumnIndex());
            if(cellWidth < cellWidthOfFirstRow) {
                // 열에 대해 추가 너비 설정
                sheet.setColumnWidth(i, cellWidthOfFirstRow);
            }

        }
    }

    /* 행 생성(Body : 넓이 자동 조절없음, 생성시간 짧음) */
    private void createBodyRow(Sheet sheet, List<String> cellList, int rowNum) {
        int size = cellList.size();
        Row row = sheet.createRow(rowNum);
        // 스타일 생성
        CellStyle style = workbook.createCellStyle();
        // 테두리 설정
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < size; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(cellList.get(i));
            // 셀에 스타일 적용
            cell.setCellStyle(style);
        }
    }

    /* 행 생성(Head) */
    private void createHeadRow(Sheet sheet, List<String> cellList, int rowNum) {
        int size = cellList.size();
        Row row = sheet.createRow(rowNum);
        // 스타일 생성
        CellStyle style = workbook.createCellStyle();
        // 셀 배경색 설정
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 테두리 설정
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 가운데 정렬
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        // 눈금선 제거
        sheet.setDisplayGridlines(false);
        // 모든 열에 대해 추가 너비 설정
        int additionalWidth = 3000;
        for (int i = 0; i < size; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(cellList.get(i));
            // 모든 열에 대해 셀의 너비 자동 조정
            sheet.autoSizeColumn(i);
            // 모든 열에 대해 추가 너비 설정값 추가하여 지정
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + additionalWidth);
            // 셀에 스타일 적용
            cell.setCellStyle(style);
        }
    }

    /* 모델 객체에 담을 형태로 엑셀 데이터 생성 */
    public static Map<String, Object> createExcelData(List<String> head, List<List<String>> body, String fileName) {
        Map<String, Object> excelData = new HashMap<>();
        try {
            excelData.put("filename", java.net.URLEncoder.encode(fileName, "UTF-8"));
            excelData.put("head", head);
            excelData.put("body", body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return excelData;
    }
}
