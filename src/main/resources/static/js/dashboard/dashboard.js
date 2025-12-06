/**
 * Dashboard Charts JavaScript
 * Contains all ApexCharts configurations for the dashboard
 */

// Format currency helper
function formatCurrency(val) {
  return (
    "$ " +
    val.toLocaleString("es-ES", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })
  );
}

// Initialize all dashboard charts
function initDashboardCharts(data) {
  initMainTrendChart(data);
  initBalanceChart(data);
  initExpenseDonutChart(data);
  initSavingsGoalsChart(data);
  initBudgetChart(data);
}

// ====== 1. MAIN TREND CHART ======
function initMainTrendChart(data) {
  const {
    labels,
    dataIngresos,
    dataEgresos,
    dataAhorros,
    ingresosBreakdown,
    egresosBreakdown,
    ahorrosBreakdown,
  } = data;

  var options = {
    series: [
      { name: "Ingresos", data: dataIngresos },
      { name: "Egresos", data: dataEgresos },
      { name: "Ahorros", data: dataAhorros },
    ],
    chart: {
      type: "line",
      height: 320,
      toolbar: {
        show: true,
        tools: {
          download: true,
          selection: false,
          zoom: false,
          zoomin: false,
          zoomout: false,
          pan: false,
          reset: false,
        },
      },
      animations: { enabled: true, easing: "easeinout", speed: 600 },
    },
    stroke: { curve: "straight", width: 2 },
    markers: {
      size: 6,
      strokeWidth: 2,
      strokeColors: "#fff",
      hover: { size: 8 },
    },
    xaxis: {
      categories: labels,
      labels: { style: { fontSize: "12px", fontWeight: 500 } },
    },
    yaxis: {
      title: { text: "Monto ($)" },
      labels: {
        formatter: (val) => "$ " + Math.round(val).toLocaleString("es-ES"),
      },
    },
    colors: ["#198754", "#dc3545", "#ffc107"],
    legend: {
      position: "top",
      horizontalAlign: "center",
      fontSize: "13px",
      markers: { width: 12, height: 12, radius: 2 },
    },
    tooltip: {
      shared: true,
      intersect: false,
      custom: function ({ series, dataPointIndex, w }) {
        const day = w.globals.categoryLabels[dataPointIndex];
        const ingreso = series[0][dataPointIndex];
        const egreso = series[1][dataPointIndex];
        const ahorro = series[2][dataPointIndex];

        const ingConceptList = ingresosBreakdown[dataPointIndex] || [];
        const egrConceptList = egresosBreakdown[dataPointIndex] || [];
        const ahoConceptList = ahorrosBreakdown[dataPointIndex] || [];

        const buildList = (list) =>
          list.length > 0
            ? list
                .map(
                  (c) =>
                    `<div style="margin-left: 18px; font-size: 11px; color: #aaa;">• ${c}</div>`
                )
                .join("")
            : "";

        return `
          <div style="background: #333; color: #fff; padding: 10px 14px; border-radius: 6px; font-size: 13px; max-width: 280px;">
            <div style="font-weight: 600; margin-bottom: 8px; color: #ccc; border-bottom: 1px solid #555; padding-bottom: 6px;">Día ${day}</div>
            <div style="margin-bottom: 6px;">
              <div style="display: flex; align-items: center;">
                <span style="display: inline-block; width: 10px; height: 10px; background: #198754; margin-right: 8px;"></span>
                <span>Ingresos: <strong>${formatCurrency(
                  ingreso
                )}</strong></span>
              </div>
              ${buildList(ingConceptList)}
            </div>
            <div style="margin-bottom: 6px;">
              <div style="display: flex; align-items: center;">
                <span style="display: inline-block; width: 10px; height: 10px; background: #dc3545; margin-right: 8px;"></span>
                <span>Egresos: <strong>${formatCurrency(egreso)}</strong></span>
              </div>
              ${buildList(egrConceptList)}
            </div>
            <div>
              <div style="display: flex; align-items: center;">
                <span style="display: inline-block; width: 10px; height: 10px; background: #ffc107; margin-right: 8px;"></span>
                <span>Ahorros: <strong>${formatCurrency(ahorro)}</strong></span>
              </div>
              ${buildList(ahoConceptList)}
            </div>
          </div>
        `;
      },
    },
    grid: { borderColor: "#e7e7e7", strokeDashArray: 4 },
  };

  var chart = new ApexCharts(
    document.querySelector("#mainTrendChart"),
    options
  );
  chart.render();
}

// ====== 2. BALANCE BAR CHART ======
function initBalanceChart(data) {
  const { labels, dataBalance, dataIngresos, dataEgresos, dataAhorros } = data;

  // Calculate dynamic range based on actual data
  const maxVal = Math.max(...dataBalance.map(Math.abs), 1);
  const minVal = Math.min(...dataBalance);
  const rangeMax = maxVal * 1.1;
  const rangeMin = minVal < 0 ? minVal * 1.1 : -rangeMax * 0.1;

  var options = {
    series: [{ name: "Balance Disponible", data: dataBalance }],
    chart: {
      type: "bar",
      height: 280,
      toolbar: {
        show: true,
        tools: {
          download: true,
          selection: true,
          zoom: true,
          zoomin: true,
          zoomout: true,
          pan: true,
          reset: true,
        },
      },
      zoom: {
        enabled: true,
        type: "x",
        autoScaleYaxis: true,
      },
      animations: { enabled: true, easing: "easeinout", speed: 600 },
    },
    plotOptions: {
      bar: {
        borderRadius: 1, // Reduced from 4 to prevent "circle" look on small bars
        columnWidth: "50%",
        colors: {
          ranges: [
            { from: -Infinity, to: -0.01, color: "#dc3545" },
            { from: 0, to: Infinity, color: "#198754" },
          ],
        },
      },
    },
    dataLabels: { enabled: false },
    xaxis: { categories: labels, labels: { style: { fontSize: "10px" } } },
    yaxis: {
      title: { text: "Balance ($)", style: { fontSize: "11px" } },
      labels: {
        formatter: (val) => "$ " + Math.round(val).toLocaleString("es-ES"),
        style: { fontSize: "10px" },
      },
    },
    tooltip: {
      custom: function ({ series, seriesIndex, dataPointIndex, w }) {
        const day = labels[dataPointIndex];
        const balance = dataBalance[dataPointIndex];
        const ingreso = dataIngresos[dataPointIndex] || 0;
        const egreso = dataEgresos[dataPointIndex] || 0;
        const ahorro = dataAhorros[dataPointIndex] || 0;

        return `
          <div style="background: #333; color: #fff; padding: 10px 14px; border-radius: 6px; font-size: 13px; min-width: 200px;">
            <div style="font-weight: 600; margin-bottom: 8px; color: #ccc; border-bottom: 1px solid #555; padding-bottom: 6px;">Día ${day}</div>
            <div style="margin-bottom: 4px;">
              <span style="display: inline-block; width: 10px; height: 10px; background: #198754; margin-right: 8px;"></span>
              Ingresos: <strong>${formatCurrency(ingreso)}</strong>
            </div>
            <div style="margin-bottom: 4px;">
              <span style="display: inline-block; width: 10px; height: 10px; background: #dc3545; margin-right: 8px;"></span>
              Egresos: <strong>${formatCurrency(egreso)}</strong>
            </div>
            <div style="margin-bottom: 4px;">
              <span style="display: inline-block; width: 10px; height: 10px; background: #ffc107; margin-right: 8px;"></span>
              Ahorros: <strong>${formatCurrency(ahorro)}</strong>
            </div>
            <div style="margin-top: 8px; padding-top: 6px; border-top: 1px solid #555;">
              <strong style="color: ${balance >= 0 ? "#198754" : "#dc3545"}">
                Balance: ${formatCurrency(balance)}
              </strong>
            </div>
          </div>
        `;
      },
    },
    grid: { borderColor: "#e7e7e7", strokeDashArray: 4 },
  };

  var chart = new ApexCharts(
    document.querySelector("#balanceTrendChart"),
    options
  );
  chart.render();
}

// ====== 3. EXPENSE DONUT CHART ======
function initExpenseDonutChart(data) {
  const { expenseNames, expenseAmounts } = data;

  var options = {
    series: expenseAmounts.length > 0 ? expenseAmounts : [1],
    chart: { type: "donut", height: 320 },
    labels: expenseNames.length > 0 ? expenseNames : ["Sin datos"],
    colors: ["#dc3545", "#fd7e14", "#ffc107", "#198754", "#0dcaf0", "#6c757d"],
    legend: { position: "bottom", fontSize: "12px" },
    dataLabels: {
      enabled: true,
      formatter: (val) => val.toFixed(1) + "%",
      style: { fontSize: "11px", fontWeight: 600 },
    },
    plotOptions: {
      pie: {
        donut: {
          size: "65%",
          labels: {
            show: true,
            name: { show: true, fontSize: "14px", fontWeight: 600 },
            value: {
              show: true,
              fontSize: "18px",
              fontWeight: 700,
              formatter: (val) => formatCurrency(parseFloat(val)),
            },
            total: {
              show: true,
              label: "Total",
              fontSize: "12px",
              fontWeight: 600,
              color: "#6c757d",
              formatter: (w) =>
                formatCurrency(
                  w.globals.seriesTotals.reduce((a, b) => a + b, 0)
                ),
            },
          },
        },
      },
    },
    tooltip: { y: { formatter: formatCurrency } },
  };

  var chart = new ApexCharts(
    document.querySelector("#expenseDonutChart"),
    options
  );
  chart.render();
}

// ====== 4. SAVINGS GOALS CHART ======
function initSavingsGoalsChart(data) {
  const { savingsGoalNames, savingsGoalTargets, savingsGoalAccumulated } = data;

  var options = {
    series: [
      { name: "Acumulado", data: savingsGoalAccumulated },
      { name: "Meta", data: savingsGoalTargets },
    ],
    chart: {
      type: "bar",
      height: 280,
      toolbar: { show: false },
      stacked: false,
    },
    plotOptions: {
      bar: { horizontal: true, borderRadius: 4, barHeight: "60%" },
    },
    dataLabels: { enabled: false },
    xaxis: {
      categories: savingsGoalNames,
      labels: {
        formatter: (val) => "$ " + Math.round(val).toLocaleString("es-ES"),
        style: { fontSize: "10px" },
      },
    },
    yaxis: { labels: { style: { fontSize: "11px" } } },
    colors: ["#ffc107", "#e9ecef"],
    legend: { position: "top", horizontalAlign: "center", fontSize: "12px" },
    tooltip: { y: { formatter: formatCurrency } },
    grid: { borderColor: "#e7e7e7", strokeDashArray: 4 },
    noData: {
      text: "No hay metas de ahorro activas",
      align: "center",
      verticalAlign: "middle",
      style: { color: "#6c757d", fontSize: "14px" },
    },
  };

  var chart = new ApexCharts(document.querySelector("#savingsChart"), options);
  chart.render();
}

// ====== 5. BUDGET PROGRESS CHART ======
function initBudgetChart(data) {
  const { budgetNames, budgetLimits, budgetSpent } = data;

  if (budgetNames.length === 0) {
    // Show no data message
    var options = {
      series: [],
      chart: { type: "bar", height: 220 },
      noData: {
        text: "No hay presupuestos activos",
        align: "center",
        verticalAlign: "middle",
        style: { color: "#6c757d", fontSize: "14px" },
      },
    };
    var chart = new ApexCharts(document.querySelector("#budgetChart"), options);
    chart.render();
    return;
  }

  // Calculate percentages (0-100+ scale)
  const percentages = budgetLimits.map((limit, i) => {
    if (limit === 0) return 0;
    return Math.min((budgetSpent[i] / limit) * 100, 150); // Cap at 150% for display
  });

  // Determine colors based on percentage
  const colors = percentages.map((pct) => {
    if (pct >= 100) return "#dc3545"; // Red - exceeded
    if (pct >= 75) return "#ffc107"; // Yellow - warning
    return "#198754"; // Green - safe
  });

  var options = {
    series: [
      {
        name: "Progreso",
        data: percentages,
      },
    ],
    chart: {
      type: "bar",
      height: 220,
      toolbar: { show: false },
      animations: { enabled: true, easing: "easeinout", speed: 600 },
    },
    plotOptions: {
      bar: {
        horizontal: true,
        borderRadius: 6,
        barHeight: "70%",
        distributed: true,
      },
    },
    colors: colors,
    dataLabels: {
      enabled: true,
      formatter: function (val, opt) {
        const spent = budgetSpent[opt.dataPointIndex];
        const limit = budgetLimits[opt.dataPointIndex];
        return formatCurrency(spent) + " / " + formatCurrency(limit);
      },
      textAnchor: "start",
      offsetX: 5,
      style: { fontSize: "10px", fontWeight: 600, colors: ["#333"] },
    },
    xaxis: {
      categories: budgetNames,
      min: 0,
      max: 100,
      tickAmount: 4,
      labels: {
        formatter: (val) => val + "%",
        style: { fontSize: "10px" },
      },
    },
    yaxis: { labels: { style: { fontSize: "11px", fontWeight: 500 } } },
    legend: { show: false },
    tooltip: {
      custom: function ({ series, seriesIndex, dataPointIndex, w }) {
        const name = budgetNames[dataPointIndex];
        const spent = budgetSpent[dataPointIndex];
        const limit = budgetLimits[dataPointIndex];
        const pct = limit > 0 ? ((spent / limit) * 100).toFixed(1) : 0;
        const remaining = limit - spent;
        const status =
          pct >= 100
            ? "⚠️ Excedido"
            : pct >= 75
            ? "⚡ Atención"
            : "✓ En control";

        return `
          <div style="background: #333; color: #fff; padding: 10px 14px; border-radius: 6px; font-size: 13px;">
            <div style="font-weight: 600; margin-bottom: 6px; border-bottom: 1px solid #555; padding-bottom: 4px;">${name}</div>
            <div style="margin-bottom: 4px;">Gastado: <strong>${formatCurrency(
              spent
            )}</strong></div>
            <div style="margin-bottom: 4px;">Límite: <strong>${formatCurrency(
              limit
            )}</strong></div>
            <div style="margin-bottom: 4px;">Disponible: <strong style="color: ${
              remaining >= 0 ? "#198754" : "#dc3545"
            }">${formatCurrency(remaining)}</strong></div>
            <div style="margin-top: 6px; font-weight: 600;">${status} (${pct}%)</div>
          </div>
        `;
      },
    },
    grid: { borderColor: "#e7e7e7", strokeDashArray: 4, padding: { left: 10 } },
    annotations: {
      xaxis: [
        {
          x: 100,
          borderColor: "#dc3545",
          strokeDashArray: 0,
          label: {
            borderColor: "#dc3545",
            style: { color: "#fff", background: "#dc3545", fontSize: "10px" },
            text: "Límite",
            position: "top",
          },
        },
      ],
    },
  };

  var chart = new ApexCharts(document.querySelector("#budgetChart"), options);
  chart.render();
}
