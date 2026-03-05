import {Box, Container, Grid} from '@mui/material';
import HeaderScreen from "../../components/HeaderScreen.tsx";
import LineChartComponent from "../../components/graph/LineChartComponent.tsx";
import BarChartComponent from "../../components/graph/BarChartComponent.tsx";
import GaugeComponent from "../../components/graph/GaugeComponent.tsx";
import {useEffect, useState} from "react";
import axiosInstance from "../api/axiosConfig.ts";

function Home() {
  // State for gauge, line chart, and bar chart filters
  const [timeGauge, setTimeGauge] = useState('7d');
  const [sportGauge, setSportGauge] = useState('all');
  const [timeLineChart, setTimeLineChart] = useState('7d');
  const [sportLineChart, setSportLineChart] = useState('all');
  const [timeBarChart, setTimeBarChart] = useState('7d');
  const [sportBarChart, setSportBarChart] = useState('all');
  // State for gauge data
  const [kmTotal, setKmTotal] = useState(0);
  const [kmObiectiv, setKmObiectiv] = useState(0);
  const [monthlyAvarage, setMonthlyAvarage] = useState({
    month: "",
    avarage: "",
    elevationGain: "",
  });
  const [monthlyElevationGain, setMonthlyElevationGain] = useState({
    month: "",
    elevationGain: "",
  });

  // Handle filter changes
  const handleValueGauge = (newTime: any) => {
    setTimeGauge(newTime);
  }
  const handleValueLineChart = (newTime: any) => {
    setTimeLineChart(newTime);
  }
  const handleValueBarChart = (newTime: any) => {
    setTimeBarChart(newTime);
  }

  const handleSportGauge = (newSport: any) => {
    setSportGauge(newSport);
  }

  const handleSportLineChart = (newSport: any) => {
    setSportLineChart(newSport);
  }

  const handleSportBarChart = (newSport: any) => {
    setSportBarChart(newSport);
  }


  //for gauge
  useEffect(() => {
    axiosInstance.get('api/strava/total-km', {
      params: {
        sportType: sportGauge,
      }
    })
      .then((response: any) => {
        if (timeGauge === "7d") {
          setKmObiectiv(40)
          setKmTotal(response.data.total_km_last_seven_day)
        } else if (timeGauge === "7m") {
          setKmObiectiv(105);
          setKmTotal(response.data.total_km_last_month)
        } else {
          setKmObiectiv(1100)
          setKmTotal(response.data.total_km_year)
        }
      })
  }, [timeGauge, sportGauge]);

  //lineChart
  useEffect(() => {
    if (timeLineChart === "7d") {
      axiosInstance.get('api/strava/performance/weekly', {
        params: {
          sportType: sportLineChart,
        }
      })
        .then((response: any) => {
          const jsonData = response.data;
          const chartData = Object.keys(jsonData).map(month => ({
            month,
            averageSpeed: jsonData[month].average_speed || 0
          })) as any;
          setMonthlyAvarage(chartData);
        })
    } else if (timeLineChart === "7m") {
      axiosInstance.get('api/strava/performance/last-7-months', {
        params: {
          sportType: sportLineChart,
        }
      })
        .then((response: any) => {
          const jsonData = response.data;
          const chartData = Object.keys(jsonData).map(month => ({
            month,
            averageSpeed: jsonData[month].average_speed || 0
          })) as any;
          setMonthlyAvarage(chartData);
        })
    } else {
      axiosInstance.get('api/strava/performance/yearly', {
        params: {
          sportType: sportLineChart,
        }
      })
        .then((response: any) => {
          const jsonData = response.data;
          const chartData = Object.keys(jsonData).map(month => ({
            month,
            averageSpeed: jsonData[month].average_speed || 0
          })) as any;
          setMonthlyAvarage(chartData);
        })
        .catch(error => {
          console.error("Errore nel recupero dati:", error);
        })
      return;
    }
  }, [timeLineChart, sportLineChart]);

  //barChart
  useEffect(() => {
    if (timeBarChart === "7d") {
      axiosInstance.get('api/strava/performance/weekly', {
        params: {
          sportType: sportBarChart,
        }
      })
        .then((response: any) => {
          const jsonData = response.data;
          const chartData = Object.keys(jsonData).map(month => ({
            month,
            elevationGain: jsonData[month].total_elevation_gain || 0
          })) as any;
          setMonthlyElevationGain(chartData);
        })
    } else if (timeBarChart === "1m") {
      axiosInstance.get('api/strava/performance/last-7-months', {
        params: {
          sportType: sportBarChart,
        }
      })
        .then((response: any) => {
          const jsonData = response.data;
          const chartData = Object.keys(jsonData).map(month => ({
            month,
            elevationGain: jsonData[month].total_elevation_gain || 0
          })) as any;
          setMonthlyElevationGain(chartData);
        })
    } else {
      axiosInstance.get('api/strava/performance/yearly', {
        params: {
          sportType: sportBarChart,
        }
      })
        .then((response: any) => {
          const jsonData = response.data;
          const chartData = Object.keys(jsonData).map(month => ({
            month,
            elevationGain: jsonData[month].total_elevation_gain || 0
          })) as any;
          setMonthlyElevationGain(chartData);
        })
        .catch(error => {
          console.error("Errore nel recupero dati:", error);
        })
      return;
    }
  }, [timeBarChart, sportBarChart]);

  return (
    <Box sx={{flexGrow: 1}}>
      <HeaderScreen title="Dashboard Analisi Dati"/>

      <Container maxWidth="lg" sx={{mt: 4, mb: 4}}>
        <Grid container spacing={3}>
          {/* Gauge Component */}
          <Grid item xs={12} md={4}>
            <GaugeComponent
              value={kmTotal}
              title="Obbiettivo"
              subtitle="Percentuale di km raggiunti"
              min={0}
              max={kmObiectiv}
              colors={['#00C49F', '#EEEEEE']}
              valueTime={handleValueGauge}
              valueSport={handleSportGauge}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <LineChartComponent
              data={Array.isArray(monthlyAvarage) ? monthlyAvarage : []}
              title="Velocita media"
              subtitle="velocita media in km/h"
              dataKey="km/h"
              valueTime={handleValueLineChart}
              valueSport={handleSportLineChart}
            />
          </Grid>

          {/* Bar Chart Component */}
          <Grid item xs={12} md={6}>
            <BarChartComponent
              data={Array.isArray(monthlyElevationGain) ? monthlyElevationGain : []}
              title="Dislivello"
              subtitle="dislivello medio"
              dataKey=""
              valueTime={handleValueBarChart}
              valueSport={handleSportBarChart}
            />
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
}

export default Home;