package com.timekeeping.dtos;

import java.sql.Date;
import java.sql.Time;

/**
 * Provider-neutral row consumed by {@code reports/dtrNew.jrxml}.
 *
 * <p>The report template is intentionally layout-only. Database retrieval and
 * cross-midnight row expansion are performed in the TimeKeeping service so the
 * same report works with SQL Server and PostgreSQL.</p>
 */
public class DtrReportRow {
    private final Date dtrDate;
    private final String fullname;
    private final Time in1st;
    private final Time out1st;
    private final Time in2nd;
    private final Time out2nd;
    private final Integer regMin;
    private final Integer excessMin;
    private final Double ndApproved;
    private final Double otReg;
    private final Double otExcess;
    private final Double otRegApproved;
    private final Double otExcessApproved;
    private final Integer lateMin;
    private final Integer underMin;
    private final Integer absentMin;
    private final String ob;
    private final String ot;
    private final Date obDate;
    private final Date otDate;
    private final String dayOff;
    private final Integer underMinHours;
    private final String remarks;
    private final String leaveType;
    private final Time nextDayholder;
    private final Boolean isForNextDay;
    private final String cto;
    private final String dayDescription;

    public DtrReportRow(
            Date dtrDate,
            String fullname,
            Time in1st,
            Time out1st,
            Time in2nd,
            Time out2nd,
            Integer regMin,
            Integer excessMin,
            Double ndApproved,
            Double otReg,
            Double otExcess,
            Double otRegApproved,
            Double otExcessApproved,
            Integer lateMin,
            Integer underMin,
            Integer absentMin,
            String ob,
            String ot,
            Date obDate,
            Date otDate,
            String dayOff,
            Integer underMinHours,
            String remarks,
            String leaveType,
            Time nextDayholder,
            Boolean isForNextDay,
            String cto,
            String dayDescription) {
        this.dtrDate = dtrDate;
        this.fullname = fullname;
        this.in1st = in1st;
        this.out1st = out1st;
        this.in2nd = in2nd;
        this.out2nd = out2nd;
        this.regMin = regMin;
        this.excessMin = excessMin;
        this.ndApproved = ndApproved;
        this.otReg = otReg;
        this.otExcess = otExcess;
        this.otRegApproved = otRegApproved;
        this.otExcessApproved = otExcessApproved;
        this.lateMin = lateMin;
        this.underMin = underMin;
        this.absentMin = absentMin;
        this.ob = ob;
        this.ot = ot;
        this.obDate = obDate;
        this.otDate = otDate;
        this.dayOff = dayOff;
        this.underMinHours = underMinHours;
        this.remarks = remarks;
        this.leaveType = leaveType;
        this.nextDayholder = nextDayholder;
        this.isForNextDay = isForNextDay;
        this.cto = cto;
        this.dayDescription = dayDescription;
    }

    public Date getDtrDate() { return dtrDate; }
    public String getFullname() { return fullname; }
    public Time getIn1st() { return in1st; }
    public Time getOut1st() { return out1st; }
    public Time getIn2nd() { return in2nd; }
    public Time getOut2nd() { return out2nd; }
    public Integer getRegMin() { return regMin; }
    public Integer getExcessMin() { return excessMin; }
    public Double getNdApproved() { return ndApproved; }
    public Double getOtReg() { return otReg; }
    public Double getOtExcess() { return otExcess; }
    public Double getOtRegApproved() { return otRegApproved; }
    public Double getOtExcessApproved() { return otExcessApproved; }
    public Integer getLateMin() { return lateMin; }
    public Integer getUnderMin() { return underMin; }
    public Integer getAbsentMin() { return absentMin; }
    public String getOb() { return ob; }
    public String getOt() { return ot; }
    public Date getObDate() { return obDate; }
    public Date getOtDate() { return otDate; }
    public String getDayOff() { return dayOff; }
    public Integer getUnderMinHours() { return underMinHours; }
    public String getRemarks() { return remarks; }
    public String getLeaveType() { return leaveType; }
    public Time getNextDayholder() { return nextDayholder; }
    public Boolean getIsForNextDay() { return isForNextDay; }
    public String getCto() { return cto; }
    public String getDayDescription() { return dayDescription; }
}
