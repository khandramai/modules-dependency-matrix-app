package org.folio.modulesdependencymatrixapp.writer.impl;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.folio.modulesdependencymatrixapp.entity.Dependency;
import org.folio.modulesdependencymatrixapp.entity.Module;
import org.folio.modulesdependencymatrixapp.writer.Writer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


@Component
public class ExcelWriter implements Writer {

    private static final String SHEET_NAME = "Export";
    private static final String[] tableFields = {"Module Id", "Module name", "Previous Release version", "Previous release date", "Previous Final Interface Versions Required", "Current Interface Versions Required (at master)", "Module Interface Owner", "RMB"};
    private static final int HEADER_FONT_SIZE = 11;
    private static final int INFO_START_COLUMN = 0;
    private static CellStyle MAJOR_CHANGED;
    private static CellStyle MINOR_CHANGED;

    private static Workbook createWorkbookAndSheet() {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet(ExcelWriter.SHEET_NAME);
        return workbook;
    }

    private static void createAndFillDefaultSheet(Workbook workbook, final List<Module> modules, Map<String, Dependency> map) {
        Sheet sheet = workbook.getSheet(ExcelWriter.SHEET_NAME);

        Font headerFont = createFont(workbook, true);
        Font infoFont = createFont(workbook, false);

        CellStyle centeredHeaderCellStyle = createHeaderCellStyle(workbook, headerFont);
        CellStyle eventInfoCellStyle = createEventInfoCellStyle(workbook, infoFont);

        MAJOR_CHANGED = createStyleWithFillColor(workbook, infoFont, IndexedColors.RED.getIndex());
        MINOR_CHANGED = createStyleWithFillColor(workbook, infoFont, IndexedColors.YELLOW.getIndex());

        setArchiveHeaders(sheet, centeredHeaderCellStyle);
        setModuleInfo(sheet, eventInfoCellStyle, modules, map);
        setArchiveColumnsWidth(sheet);
    }

    private static void setModuleInfo(final Sheet sheet, final CellStyle timeSlotInfoCellStyle, final List<Module> modules, Map<String, Dependency> map) {
        AtomicInteger rowIndex = new AtomicInteger(0);
        modules.forEach(moduleEntry -> {
//TODO
//            sheet.addMergedRegion(new CellRangeAddress(row, max, 0, 0));
//            sheet.addMergedRegion(new CellRangeAddress(row, max, 1, 1));
//            sheet.addMergedRegion(new CellRangeAddress(row, max, 5, 5));
            addModuleInfo(sheet, timeSlotInfoCellStyle, rowIndex, moduleEntry, map);

        });
    }

    private static void addModuleInfo(Sheet sheet, CellStyle timeSlotInfoCellStyle, AtomicInteger rowIndex, Module module, Map<String, Dependency> map) {
        AtomicInteger cellIndex = new AtomicInteger(0);

        Row row = getOrCreateRow(sheet, rowIndex);

        createCell(row, cellIndex.getAndIncrement(), module.getName(), timeSlotInfoCellStyle);
        createCell(row, cellIndex.getAndIncrement(), module.getArtifactId(), timeSlotInfoCellStyle);
        createCell(row, cellIndex.getAndIncrement(), module.getPreviousReleaseVersion(), timeSlotInfoCellStyle);
        createCell(row, cellIndex.getAndIncrement(), module.getPreviousReleaseData(), timeSlotInfoCellStyle);
        List<Dependency> requires = module.getRequires();

        AtomicReference<Row> rowInc = new AtomicReference<>();


        if (Objects.nonNull(requires)) {
            requires.forEach(el -> {
                CellStyle currentStyle = timeSlotInfoCellStyle;
                rowInc.set(getOrCreateRow(sheet, rowIndex));
                if (map.containsKey(el.getId())) {
                    Dependency dependency = map.get(el.getId());
                    if(el.getVersion().isMajorChanged(dependency.getVersion())){
                        currentStyle = MAJOR_CHANGED;
                    }else if(el.getVersion().isMinorChanged(dependency.getVersion())){
                        currentStyle = MINOR_CHANGED;
                    }
                    createCell(rowInc.get(), cellIndex.incrementAndGet(), dependency.getId() + ":: " + dependency.getVersion(), currentStyle);
                    createCell(rowInc.get(), cellIndex.incrementAndGet(), dependency.getOwnerName(), currentStyle);
                    cellIndex.set(cellIndex.get() - 2);
                }
                createCell(rowInc.get(), cellIndex.get(), el.getId() + ":: " + el.getVersion(), currentStyle);

            });
        }
        cellIndex.set(7);
        createCell(row, cellIndex.get(), module.getRmb(), timeSlotInfoCellStyle);

    }

    private static Row getOrCreateRow(final Sheet sheet, final AtomicInteger rowIndex) {
        Row row = sheet.getRow(rowIndex.incrementAndGet());
        if (Objects.isNull(row)) {
            row = sheet.createRow(rowIndex.get());
        }
        return row;
    }

    private static void createCell(final Row row, final int cellIndex, final String data, final CellStyle cellStyle) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(data);
        cell.setCellStyle(cellStyle);
    }

    private static void setArchiveColumnsWidth(final Sheet sheet) {
        sheet.setColumnWidth(0, 95 * 40);
        sheet.setColumnWidth(1, 65 * 40);
        sheet.setColumnWidth(2, 100 * 40);
        sheet.setColumnWidth(3, 300 * 40);
        sheet.setColumnWidth(4, 300 * 40);
        sheet.setColumnWidth(5, 300 * 40);
        sheet.setColumnWidth(6, 64 * 40);
    }

    private static void setArchiveHeaders(final Sheet sheet, final CellStyle cellStyle) {
        Row archiveInfoHeadersRow = sheet.createRow(0);
        int cellIndex = INFO_START_COLUMN;
        for (String fieldName : tableFields) {
            createCell(archiveInfoHeadersRow, cellIndex++, fieldName, cellStyle);
        }
    }

    private static Font createFont(final Workbook workbook, final boolean isBold) {
        Font font = workbook.createFont();
        font.setBold(isBold);
        font.setFontHeightInPoints((short) HEADER_FONT_SIZE);
        font.setColor(IndexedColors.BLACK.getIndex());
        return font;
    }

    private static CellStyle createHeaderCellStyle(final Workbook workbook, final Font font) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        addBorder(cellStyle);
        return cellStyle;
    }

    private static CellStyle createStyleWithFillColor(final Workbook workbook, final Font font, short color) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        addBorder(cellStyle);
        return cellStyle;
    }

    private static CellStyle createEventInfoCellStyle(final Workbook workbook, final Font font) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        addBorder(cellStyle);
        return cellStyle;
    }

    private static void addBorder(final CellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
    }

    @Override
    public Workbook exportToExcel(List<Module> moduleList, Map<String, Dependency> map) {
        Workbook workbook = createWorkbookAndSheet();
        createAndFillDefaultSheet(workbook, moduleList, map);
        return workbook;
    }
}
