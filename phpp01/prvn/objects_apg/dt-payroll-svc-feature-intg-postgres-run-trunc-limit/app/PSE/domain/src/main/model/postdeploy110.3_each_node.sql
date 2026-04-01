alter system set "_b_tree_bitmap_plans" = false scope=spfile;
alter system set "_optimizer_mjc_enabled" = false scope=spfile;

shutdown immediate
startup

