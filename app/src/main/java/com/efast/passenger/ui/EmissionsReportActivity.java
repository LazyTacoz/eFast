package com.efast.passenger.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.Callback;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.GreenTrip;
import com.efast.passenger.databinding.ActivityEmissionsReportBinding;
import com.efast.passenger.databinding.ItemReportRowBinding;
import com.efast.passenger.green.Co2Calculator;
import com.efast.passenger.green.EmissionFactors;
import com.efast.passenger.green.EmissionsReport;
import com.efast.passenger.green.ReportCsv;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.FareCalculator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Monthly CO2-saved report, for one rider ("My CO2 savings") or a whole company account
 * (B2B), with a CSV the account can drop into its sustainability reporting.
 */
public class EmissionsReportActivity extends AppCompatActivity {

    private static final String EXTRA_COMPANY = "extra_company";
    private static final DateTimeFormatter MONTH_LABEL =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final RideRepository repo = AppGraph.rideRepository();
    private final Co2Calculator co2 = new Co2Calculator(EmissionFactors.indiaDefaults());
    private ActivityEmissionsReportBinding binding;
    private boolean company;
    private YearMonth month = YearMonth.now();
    private EmissionsReport report;
    /** Bumped on every load, so a slow answer for a month we've already left is ignored. */
    private int loadId;

    public static Intent personal(Context context) {
        return new Intent(context, EmissionsReportActivity.class);
    }

    public static Intent company(Context context) {
        return new Intent(context, EmissionsReportActivity.class).putExtra(EXTRA_COMPANY, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEmissionsReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        company = getIntent().getBooleanExtra(EXTRA_COMPANY, false);
        binding.reportTitle.setText(company ? R.string.report_title_company : R.string.report_title_personal);
        binding.accountName.setText(company
                ? getString(R.string.report_account_company, repo.getCompanyName())
                : getString(R.string.report_account_personal));

        EmissionFactors f = co2.factors();
        binding.methodText.setText(getString(R.string.report_method, f.evWattHoursPerKm,
                f.gridGramsCo2PerKwh, f.petrolGramsCo2PerLitre, f.petrolKmPerLitreText()));

        binding.prevMonthButton.setOnClickListener(v -> load(month.minusMonths(1)));
        binding.nextMonthButton.setOnClickListener(v -> load(month.plusMonths(1)));
        binding.shareCsvButton.setOnClickListener(v -> shareCsv());

        load(month);
    }

    private void load(YearMonth newMonth) {
        month = newMonth;
        report = null;
        int id = ++loadId;
        binding.monthLabel.setText(month.format(MONTH_LABEL));
        binding.nextMonthButton.setEnabled(month.isBefore(YearMonth.now()));
        binding.progress.setVisibility(View.VISIBLE);
        binding.summaryCard.setVisibility(View.GONE);
        binding.byRiderTitle.setVisibility(View.GONE);
        binding.emptyText.setVisibility(View.GONE);
        binding.rowsContainer.removeAllViews();
        binding.shareCsvButton.setEnabled(false);

        if (!company) {
            show(AppGraph.greenRideStore(this).rides(), getString(R.string.report_title_personal));
            return;
        }
        repo.getCompanyTrips(month, new Callback<List<GreenTrip>>() {
            @Override
            public void onSuccess(List<GreenTrip> trips) {
                if (id != loadId || isDestroyed()) return;
                show(trips, repo.getCompanyName());
            }

            @Override
            public void onError(String message) {
                if (id != loadId || isDestroyed()) return;
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(EmissionsReportActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void show(List<GreenTrip> trips, String accountName) {
        report = EmissionsReport.build(accountName, month, trips, co2);
        binding.progress.setVisibility(View.GONE);

        if (report.rides == 0) {
            binding.emptyText.setText(company ? R.string.report_empty_company : R.string.report_empty_personal);
            binding.emptyText.setVisibility(View.VISIBLE);
            return;
        }

        binding.totalSaved.setText(Co2Calculator.kg(report.savedGrams));
        binding.totalDetail.setText(getString(R.string.report_total_detail,
                ridesText(report.rides), FareCalculator.km(report.distanceMeters)));
        binding.baselineDetail.setText(getString(R.string.report_baseline_detail,
                Co2Calculator.kg(report.petrolCarGrams), Co2Calculator.kg(report.evGrams)));
        binding.summaryCard.setVisibility(View.VISIBLE);

        // A personal report has one row ("You"), which would just repeat the summary.
        if (company) {
            binding.byRiderTitle.setVisibility(View.VISIBLE);
            for (EmissionsReport.Row row : report.rows) {
                ItemReportRowBinding item = ItemReportRowBinding.inflate(
                        getLayoutInflater(), binding.rowsContainer, false);
                item.riderName.setText(row.riderName);
                item.riderDetail.setText(getString(R.string.report_row_detail,
                        ridesText(row.rides), FareCalculator.km(row.distanceMeters)));
                item.riderSaved.setText(Co2Calculator.kg(row.savedGrams));
                binding.rowsContainer.addView(item.getRoot());
            }
        }
        binding.shareCsvButton.setEnabled(true);
    }

    private String ridesText(int rides) {
        return getResources().getQuantityString(R.plurals.rides_count, rides, rides);
    }

    /** Writes the CSV to cache/reports/ and opens the share sheet (email, Drive, WhatsApp, ...). */
    private void shareCsv() {
        if (report == null) return;
        try {
            File dir = new File(getCacheDir(), "reports");
            if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("mkdirs failed");
            File file = new File(dir, "efast-co2-" + (company ? "company" : "personal")
                    + "-" + report.month + ".csv");
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(ReportCsv.toCsv(report).getBytes(StandardCharsets.UTF_8));
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent send = new Intent(Intent.ACTION_SEND)
                    .setType("text/csv")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_share_subject,
                            report.accountName, report.month.format(MONTH_LABEL)))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, getString(R.string.report_share_csv)));
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(this, R.string.report_share_error, Toast.LENGTH_SHORT).show();
        }
    }
}
