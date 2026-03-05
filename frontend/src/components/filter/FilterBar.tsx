import {FormControl, InputLabel, Select, MenuItem} from '@mui/material';

const FilterBar = ({timeRange, onRangeChange}: any) => {
  const handleChange = (event: any) => {
    onRangeChange(event.target.value);
  };

  return (
    <FormControl sx={{m: 1, minWidth: 120}}>
      <InputLabel id={"periodo-select"}>Periodo</InputLabel>
      <Select
        labelId="periodo-select"
        data-testid="time-range-select"
        value={timeRange}
        label="Periodo"
        onChange={handleChange}
      >
        <MenuItem value="7d">Ultimi 7 giorni</MenuItem>
        <MenuItem value="7m">Ultimi 7 mesi</MenuItem>
        <MenuItem value="1y">Ultimo anno</MenuItem>
      </Select>
    </FormControl>
  );
};

export default FilterBar;