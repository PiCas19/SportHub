import {Card, CardContent, Typography, Box} from '@mui/material';
import {PieChart, Pie, Cell, ResponsiveContainer} from 'recharts';
import FilterBar from "../filter/FilterBar.tsx";
import {useState} from "react";
import FilterSport from "../filter/FilterSport.tsx";

// GaugeComponent for displaying a semi-circle gauge chart
const GaugeComponent = ({value, title, subtitle, min = 0, max = 100, colors, valueTime, valueSport}: any) => {
  const normalizedValue = ((value - min) / (max - min)) * 100;
  const remaining = 100 - normalizedValue;

  // Default colors for gauge segments
  const defaultColors = ['#0088FE', '#EEEEEE'];
  const gaugeColors = colors || defaultColors;

  // State for time range and sport type filters
  const [timeRange, setTimeRange] = useState('7d');
  const [sportType, setSportType] = useState('all');

  // Data for pie chart
  const data = [
    {name: 'Value', value: normalizedValue},
    {name: 'Remaining', value: remaining}
  ];

  // Handle time range filter change
  const handleRangeChange = (newRange: any) => {
    setTimeRange(newRange);
    valueTime(newRange);
  };

  // Handle sport type filter change
  const handleSportChange = (newSport: any) => {
    setSportType(newSport);
    valueSport(newSport);
  };

  return (
    <Card>
      <CardContent>
        {/* Chart title and subtitle */}
        <Box sx={{alignItems: 'center', justifyContent: 'space-between'}}>
          <Typography variant="h6" gutterBottom>
            {title}
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            {subtitle}
          </Typography>
        </Box>
        {/* Filters for time range and sport type */}
        <Box sx={{display: "flex", flexDirection: "row", justifyContent: "space-between"}}>
          <FilterBar
            timeRange={timeRange}
            onRangeChange={handleRangeChange}
          />
          <FilterSport
            sportType={sportType}
            onSportChange={handleSportChange}
          />
        </Box>

        {/* Gauge chart container */}
        <Box sx={{position: 'relative', width: '100%', height: 300, minHeight: 300, minWidth: '100%'}}>
          <ResponsiveContainer width="100%" height="100%" data-testid="responsive-container">
            <PieChart data-testid="gauge-chart">
              <Pie
                data={data}
                cx="50%"
                cy="90%"
                startAngle={180}
                endAngle={0}
                innerRadius={80}
                outerRadius={120}
                paddingAngle={0}
                dataKey="value"
              >
                <Cell key={`cell-0`} fill={gaugeColors[0]}/>
                <Cell key={`cell-1`} fill={gaugeColors[1]}/>
              </Pie>
            </PieChart>
          </ResponsiveContainer>

          <Box
            sx={{
              position: 'absolute',
              top: '80%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              textAlign: 'center'
            }}
          >
            <Typography variant="h3" component="div">
              {value}
            </Typography>
          </Box>

          <Box
            sx={{
              position: 'absolute',
              bottom: '5%',
              left: '50%',
              width: '80%',
              display: 'flex',
              justifyContent: 'space-between',
              transform: 'translateX(-50%)'
            }}
          >
            <Typography variant="body2">{min}</Typography>
            <Typography variant="body2">{max} km</Typography>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export default GaugeComponent;