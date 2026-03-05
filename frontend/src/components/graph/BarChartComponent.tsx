import {Box, Card, CardContent, Typography} from '@mui/material';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';
import FilterBar from "../filter/FilterBar.tsx";
import {useMemo, useState} from "react";
import FilterSport from "../filter/FilterSport.tsx";

const MESI = ['Gen', 'Feb', 'Mar', 'Apr', 'Mag', 'Giu', 'Lug', 'Ago', 'Set', 'Ott', 'Nov', 'Dic'];
const GIORNI = ['Dom', 'Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab'];

// BarChartComponent for displaying activity data in a bar chart
const BarChartComponent = ({data, title, subtitle, dataKey, valueTime, valueSport}: any) => {
  const [timeRange, setTimeRange] = useState('7d');
  const [sportType, setSportType] = useState('all');

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

  // Get weekday name from date string
  const getWeekday = (dateString: string) => {
    const date = new Date(dateString);
    return GIORNI[date.getDay()];
  };

  // Memoized data filtering based on time range
  const filteredData = useMemo(() => {
      if (timeRange === '7d') {
        return data.map((item: any) => {
          const weekday = getWeekday(item.month);
          return {
            name: weekday,
            [dataKey]: item.elevationGain,
          };
        });
      } else if (timeRange === '7m') {
        return data
          .slice(-7)
          .map((item: any) => {
            const yearMonth = item.month.split("-");
            const monthIndex = parseInt(yearMonth[1], 10) - 1;
            return {
              name: MESI[monthIndex],
              [dataKey]: item.elevationGain,
            }
          });
      } else {
        return data.map((item: any) => {
          const yearMonth = item.month.split("-");
          const monthIndex = parseInt(yearMonth[1], 10) - 1;
          return {
            name: MESI[monthIndex],
            [dataKey]: item.elevationGain,
          }
        })
      }
      return data;
    }
    ,
    [timeRange, data, dataKey, sportType]
  );

  return (
    <Card data-testid="bar-chart">
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
        {/* Responsive bar chart */}
        <ResponsiveContainer width="100%" height={300}>
          <BarChart
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
            <Bar
              dataKey={dataKey}
              fill="#82ca9d"
              name={`Valore ${dataKey.slice(-1)}`}
            />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};

export default BarChartComponent;