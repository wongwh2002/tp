package seedu.storage;

import seedu.classes.WiagiLogger;
import seedu.commands.BudgetCommand;
import seedu.recurrence.RecurrenceFrequency;
import seedu.type.Spending;
import seedu.type.SpendingList;
import seedu.classes.Ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;

import static seedu.classes.Constants.LOAD_DAILY_BUDGET_INDEX;
import static seedu.classes.Constants.LOAD_MONTHLY_BUDGET_INDEX;
import static seedu.classes.Constants.LOAD_SPENDING_FILE_ERROR;
import static seedu.classes.Constants.LOAD_YEARLY_BUDGET_INDEX;
import static seedu.classes.Constants.SAVE_SPENDING_FILE_ERROR;
import static seedu.classes.Constants.STORAGE_LOAD_SEPARATOR;
import static seedu.classes.Constants.STORAGE_SEPARATOR;
import static seedu.classes.Constants.LOAD_AMOUNT_INDEX;
import static seedu.classes.Constants.LOAD_DATE_INDEX;
import static seedu.classes.Constants.LOAD_DAY_OF_RECURRENCE_INDEX;
import static seedu.classes.Constants.LOAD_DESCRIPTION_INDEX;
import static seedu.classes.Constants.LOAD_LAST_RECURRED_INDEX;
import static seedu.classes.Constants.LOAD_RECURRENCE_INDEX;
import static seedu.classes.Constants.LOAD_TAG_INDEX;
import static seedu.classes.Constants.NO_RECURRENCE;
import static seedu.storage.LoginStorage.PASSWORD_FILE_PATH;

/**
 * Manages saving and loading of spending data to and from a file.
 */
public class SpendingListStorage {
    static final String SPENDINGS_FILE_PATH = "./spendings.txt";

    /**
     * Saves the spending list, including each spending entry and budget details, to a file.
     *
     * @param spendings the SpendingList to be saved.
     */
    static void save(SpendingList spendings) {
        WiagiLogger.logger.log(Level.INFO, "Starting to save spendings...");
        try {
            FileWriter fw = new FileWriter(SPENDINGS_FILE_PATH);
            String budgetDetails = spendings.getDailyBudget() + STORAGE_SEPARATOR +
                    spendings.getMonthlyBudget() + STORAGE_SEPARATOR + spendings.getYearlyBudget();
            fw.write(budgetDetails + System.lineSeparator());
            for (Spending spending : spendings) {
                String singleEntry = spending.getAmount() + STORAGE_SEPARATOR + spending.getDescription() +
                        STORAGE_SEPARATOR + spending.getDate() + STORAGE_SEPARATOR + spending.getTag() +
                        STORAGE_SEPARATOR + spending.getRecurrenceFrequency() + STORAGE_SEPARATOR +
                        spending.getLastRecurrence() + STORAGE_SEPARATOR + spending.getDayOfRecurrence();
                fw.write(singleEntry + System.lineSeparator());
            }
            fw.close();
        } catch (IOException e) {
            WiagiLogger.logger.log(Level.WARNING, "Unable to save spendings file", e);
            Ui.printWithTab(SAVE_SPENDING_FILE_ERROR);
        }
        WiagiLogger.logger.log(Level.INFO, "Finish saving spendings file");
    }

    /**
     * Loads the spending data from a file into the application's spending list.
     * If no file exists, a new one is created.
     */
    static void load() {
        WiagiLogger.logger.log(Level.INFO, "Starting to load spendings...");
        try {
            if (new File(SPENDINGS_FILE_PATH).createNewFile()) {
                emptyFileErrorHandling();
                return;
            }
            File spendingFile = new File(SPENDINGS_FILE_PATH);
            Scanner spendingReader = new Scanner(spendingFile);
            String[] budgetDetails = spendingReader.nextLine().split(STORAGE_LOAD_SEPARATOR);
            loadBudgets(budgetDetails);
            while (spendingReader.hasNext()) {
                String newEntry = spendingReader.nextLine();
                addLoadingEntry(newEntry);
            }
        } catch (IOException e) {
            WiagiLogger.logger.log(Level.WARNING, "Unable to open spendings file", e);
            Ui.printWithTab(LOAD_SPENDING_FILE_ERROR);
        } catch (NoSuchElementException e) {
            WiagiLogger.logger.log(Level.WARNING, "Spendings file is empty", e);
            emptyFileErrorHandling();
        }
        WiagiLogger.logger.log(Level.INFO, "Finish loading spendings file.");
    }

    private static void addLoadingEntry(String newEntry) {
        String[] entryData = newEntry.split(STORAGE_LOAD_SEPARATOR);
        LocalDate date = LocalDate.parse(entryData[LOAD_DATE_INDEX]);
        LocalDate lastRecurred = null;
        if (!entryData[LOAD_LAST_RECURRED_INDEX].equals(NO_RECURRENCE)) {
            lastRecurred = LocalDate.parse(entryData[LOAD_LAST_RECURRED_INDEX]);
        }
        Spending nextEntry = new Spending(Double.parseDouble(entryData[LOAD_AMOUNT_INDEX]),
                entryData[LOAD_DESCRIPTION_INDEX], date, entryData[LOAD_TAG_INDEX],
                RecurrenceFrequency.valueOf(entryData[LOAD_RECURRENCE_INDEX]),
                lastRecurred, Integer.parseInt(entryData[LOAD_DAY_OF_RECURRENCE_INDEX]));
        Storage.spendings.add(nextEntry);
    }

    private static void loadBudgets(String[] budgetDetails) {
        Storage.spendings.setDailyBudget(Double.parseDouble(budgetDetails[LOAD_DAILY_BUDGET_INDEX]));
        Storage.spendings.setMonthlyBudget(Double.parseDouble(budgetDetails[LOAD_MONTHLY_BUDGET_INDEX]));
        Storage.spendings.setYearlyBudget(Double.parseDouble(budgetDetails[LOAD_YEARLY_BUDGET_INDEX]));
    }

    private static void emptyFileErrorHandling() {
        File spendingFile = new File(SPENDINGS_FILE_PATH);
        spendingFile.delete();
        if (new File(PASSWORD_FILE_PATH).exists()) {
            Ui.errorLoadingBudgetMessage();
            BudgetCommand.initialiseBudget(Storage.spendings);
        }
    }
}
