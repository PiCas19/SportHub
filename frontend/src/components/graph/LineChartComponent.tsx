import {Box, Card, CardContent, Typography} from '@mui/material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';
import {useState, useMemo} from "react";
import FilterBar from "../filter/FilterBar.tsx";
import FilterSport from "../filter/FilterSport.tsx";

// Arrays for labels in Italian
const MESI = ['Gen', 'Feb', 'Mar', 'Apr', 'Mag', 'Giu', 'Lug', 'Ago', 'Set', 'Ott', 'Nov', 'Dic'];
const GIORNI = ['Dom', 'Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab'];


const LineChartComponent = ({data, title, subtitle, dataKey, valueTime, valueSport}: any) => {
  // State for selected time range and sport type
  const [timeRange, setTimeRange] = useState('7d');
  const [sportType, setSportType] = useState('all');

  // Handlers for filter changes
  const handleRangeChange = (newRange: any) => {
    setTimeRange(newRange);
    valueTime(newRange);
  };

  const handleSportChange = (newSport: any) => {
    setSportType(newSport);
    valueSport(newSport);
  };

  // Convert date string to weekday name
  const getWeekday = (dateString: string) => {
    const date = new Date(dateString);
    return GIORNI[date.getDay()];
  };

  // Filter and transform data based on selected range
  const filteredData = useMemo(() => {
      if (timeRange === '7d') {
        return data.map((item: any) => {
          const weekday = getWeekday(item.month);
          return {
            name: weekday,
            [dataKey]: item.averageSpeed,
          };
        });
      } else if (timeRange === '7m') {
        return data.map((item: any) => {
          const yearMonth = item.month.split("-");
          const monthIndex = parseInt(yearMonth[1], 10) - 1;
          return {
            name: MESI[monthIndex],
            [dataKey]: item.averageSpeed,
          }
        })
      } else {
        return data.map((item: any) => {
          const yearMonth = item.month.split("-");
          const monthIndex = parseInt(yearMonth[1], 10) - 1;
          return {
            name: MESI[monthIndex],
            [dataKey]: item.averageSpeed,
          }
        })
      }
      return data;
    }
    ,
    [timeRange, data, dataKey, sportType]
  );

  return (
    <Card data-testid="line-chart">
      <CardContent>
        {/* Title and subtitle */}
        <Box sx={{alignItems: 'center', justifyContent: 'space-between'}}>
          <Typography variant="h6" gutterBottom>
            {title}
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            {subtitle}
          </Typography>
        </Box>

        {/* Filter controls */}
        <Box sx={{display: 'flex', flexDirection: 'row', justifyContent: "space-between"}}>
          <FilterBar
            timeRange={timeRange}
            onRangeChange={handleRangeChange}
          />
          <FilterSport
            sportType={sportType}
            onSportChange={handleSportChange}
          />
        </Box>

        {/* Chart rendering */}
        <ResponsiveContainer width="100%" height={300}>
          <LineChart
            data={filteredData}
            margin={{
              top: 5,
              right: 30,
              left: 20,
              bottom: 5,
            }}
          >
            <CartesianGrid strokeDasharray="3 3"/>
            <XAxis dataKey="name"/>
            <YAxis/>
            <Tooltip/>
            <Legend/>
            <Line
              type="monotone"
              dataKey={dataKey}
              stroke="#8884d8"
              activeDot={{r: 8}}
              name={`Valore ${dataKey.slice(-1)}`}
            />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};

export default LineChartComponent;
