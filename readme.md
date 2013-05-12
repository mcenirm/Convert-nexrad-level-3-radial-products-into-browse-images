Convert nexrad level 3 radial products into (browse images? netcdf grid files? something more nefarious?)

java -jar cydep.jar configuration_file input_file output_file

Configuration file:

    variable_name
    cutoff_value red green blue   # colormap index 1 (index 0 will be transparent)
    .
    .
    .
    cutoff_value red green blue   # colormap index N

Every value below the nth cutoff will be associated with the (n-1)th colormap index. NaN -> 0

