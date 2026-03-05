import {FormControl, InputLabel, Select, MenuItem} from '@mui/material';

const FilterSport = ({sportType, onSportChange}: any) => {

  const handleChange = (event: any) => {
    onSportChange(event.target.value);
  };

  return (
    <FormControl sx={{m: 1, minWidth: 120}}>
      <InputLabel id={"sport-label"}>Sport</InputLabel>
      <Select
        labelId={"sport-label"}
        data-testid="sport-select"
        value={sportType}
        label="SportType"
        onChange={handleChange}
      >
        <MenuItem value="all">Tutti</MenuItem>
        <MenuItem value="run">Corsa</MenuItem>
        <MenuItem value="swim">Nuoto</MenuItem>
        <MenuItem value="walk">Camminata</MenuItem>
        <MenuItem value="ride">Bici</MenuItem>
      </Select>
    </FormControl>
  );
};

export default FilterSport;