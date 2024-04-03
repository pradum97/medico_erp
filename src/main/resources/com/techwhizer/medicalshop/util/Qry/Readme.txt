For MATERIALIZED View
    Step-1 : Create MATERIALIZED
    Step-2 : Create UNIQUE Index (CREATE UNIQUE INDEX ON billing_report_v (sale_main_id);)
    Step 3 : Add MATERIALIZED View in Refresh Function (refresh_materialized_view())
    Step 4 : Add Refresh Function In Needed Trigger

If Joining in table then create trigger for  MATERIALIZED view
