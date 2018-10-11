package org.openchs.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openchs.web.request.PeriodRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Date;

public class ExcelUtil {
    private static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);
    //Do not remove any pattern because this is built up based on formats we have encountered so far in various migration data
    private static DateTimeFormatter[] possibleDateFormatters = new DateTimeFormatter[]{DateTimeFormat.forPattern("dd/MMM/yyyy"), DateTimeFormat.forPattern("dd/M/yyyy"), DateTimeFormat.forPattern("dd-MMM-yyyy"), DateTimeFormat.forPattern("dd-M-yyyy"), DateTimeFormat.forPattern("dd.M.yyyy"), DateTimeFormat.forPattern("yyyy-MMM-dd"), DateTimeFormat.forPattern("yyyy-MM-dd"), DateTimeFormat.forPattern("yyyy-MM-dd H:m:s.S"),DateTimeFormat.forPattern("yyyy-MM-dd H:m:s"),DateTimeFormat.forPattern("H:m:s")};
    private static String NAN = "NAN";

    public static String getText(Row row, int cellNum) {
        String fatText = ExcelUtil.getFatText(row, cellNum);
        return fatText == null ? null : fatText.replaceAll(" +", " ");
    }

    public static String getFatText(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;
        String stringCellValue;
        try {
            stringCellValue = cell.getStringCellValue();
        } catch (IllegalStateException e) {
            if (cell.getCellTypeEnum().equals(CellType.NUMERIC)) {
                stringCellValue = String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
            } else {
                stringCellValue = cell.toString();
            }
        }
        String trimmed = StringUtils.trimWhitespace(stringCellValue);
        return StringUtils.isEmpty(trimmed) ? null : trimmed;
    }

    public static Boolean isFirstCellEmpty(Row row) {
        try {
            Cell cell = row.getCell(0);
            if (cell == null) return true;
            return cell.getStringCellValue().trim().isEmpty();
        } catch (NullPointerException ne) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getRawCellValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;
        return ((XSSFCell) cell).getRawValue();
    }

    public static Date getDateFromDuration(String durationString, Date referenceDate) {
        return PeriodRequest.fromString(durationString).toDate(new LocalDate(referenceDate)).toDate();
    }

    public static Date getDate(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        try {
            if (cell == null) return null;
            if (cell.getCellTypeEnum() == CellType.STRING) return getDateFromString(row, cellNum);
            return cell.getDateCellValue();
        } catch (RuntimeException e) {
            logger.error(String.format("getDate failed for row_number=%d, cell_number=%d, it contains:%s", row.getRowNum(), cellNum, cell.toString()));
            throw e;
        }
    }

    public static Date getDateFromString(Row row, int cellNum) {
        String text = ExcelUtil.getText(row, cellNum);
        return getDateFromString(text);
    }

    public static Date getDateFromString(String text) {
        for (DateTimeFormatter possibleFormatter : possibleDateFormatters) {
            try {
                DateTime dateTime = possibleFormatter.parseDateTime(text);
                return dateTime.toDate();
            } catch (IllegalArgumentException ignored) {
            }
        }
        throw new IllegalArgumentException(String.format("Could not format:%s in any date format", text));
    }

    public static Double getNumber(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        try {
            if (cell == null) return null;
            if (StringUtils.isEmpty(cell.toString())) return null;
            return cell.getNumericCellValue();
        } catch (RuntimeException e) {
            if (NAN.equalsIgnoreCase(ExcelUtil.getFatText(row, cellNum))) {
                return null;
            }
            logger.error(String.format("getNumber failed for row_number=%d, cell_number=%d, it contains:%s", row.getRowNum(), cellNum, cell.toString()));
            return null;
        }
    }

    public static Boolean getBoolean(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        try {
            if (cell == null) return null;
            if (StringUtils.isEmpty(cell.toString())) return null;
            return cell.getBooleanCellValue();
        } catch (RuntimeException e) {
            logger.error(String.format("getBoolean failed for row_number=%d, cell_number=%d, it contains:%s", row.getRowNum(), cellNum, cell.toString()));
            return null;
        }
    }

    public static Object getValueOfBestType(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;

        switch (cell.getCellTypeEnum().toString()) {
            case "NUMERIC":
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return ExcelUtil.getNumber(row, cellNum);
            case "BOOLEAN":
                return ExcelUtil.getBoolean(row, cellNum);
            default:
                return ExcelUtil.getText(row, cellNum);
        }
    }
}