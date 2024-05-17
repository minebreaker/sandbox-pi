import React, { FunctionComponent, useCallback, useState, ChangeEvent } from "react"
import { useEffectAsync } from "../react/useEffectAsync"
import Highcharts from 'highcharts'
import HC_more from "highcharts/highcharts-more"
import HighchartsReact from "highcharts-react-official"
import _ from "lodash"
import { createUseStyles } from "react-jss"

HC_more(Highcharts)

type Room = {
  id: string
  name: string
}

type RoomResponse = {
  items: Room[]
}

function getChartOptions(dataMed: any, dataMinMax: any, name: string, unit: string): Highcharts.Options {
  return {
    title: {
      text: name
    },
    xAxis: {},
    yAxis: {
      title: {
        text: unit
      }
    },
    series: [
      {
        name: name,
        data: dataMed,
        marker: {},
        connectNulls: true
      } as any,
      {
        name: "Max-Min",
        type: "arearange",
        data: dataMinMax,
        lineWidth: 0,
        color: Highcharts.getOptions().colors!![0],
        fillOpacity: 0.3,
        marker: { enabled: false },
        connectNulls: true
      }
    ]
  }
}

const useChartContainerStyles = createUseStyles({
  container: {
    width: "100dvw",
    height: "100dvh",
    display: "grid",
    gridTemplateColumns: "1fr 1fr"
  },
  item: {
    width: "50dvw",
    height: "50dvh"
  },
  "@media only screen and (max-width: 768px)": {
    container: {
      gridTemplateColumns: "1fr"
    },
    item: {
      width: "100dvw"
    }
  }
})

export type ChartProps = {}

export const Chart: FunctionComponent<ChartProps> = props => {

  const chartStyles = useChartContainerStyles()

  const [rooms, setRooms] = useState<Room[]>([])
  const [selectedRoom, setSelectedRoom] = useState<Room | undefined>()
  const [charts, setCharts] = useState<Highcharts.Options[]>([])

  useEffectAsync(async () => {

    const response = await fetch(`/room`)
    if (response.ok) {
      const body = await response.json() as RoomResponse // TODO
      setRooms(body.items)
    } else {
      throw new Error("TODO")
    }
  }, [])

  const onRoomChange = useCallback(async (e: ChangeEvent<HTMLSelectElement>) => {
    const room = rooms.find(r => r.id === e.target.value)
    if (!room) {
      console.warn(`couldn't find room ${e.target.value}`)
      return
    }

    setSelectedRoom(room)

    const response = await fetch(`/stat?room_id=${room.id}&p=day`)
    if (response.ok) {
      const body = await response.json() // FIXME

      const charts = [
        [body.temperature, "Temperature", "℃", (n: number) => n],
        [body.pressure, "Pressure", "㍱", (n: number) => n / 100],
        [body.humidity, "Humidity", "％", (n: number) => n],
        [body.co2, "CO2", "㏙", (n: number) => n],
        [body.smell, "Smell", "V", (n: number) => n / 1_000_000]
      ].map(([data, name, unit, convert]) => {
        const arr = Object.entries(data)
        return getChartOptions(
          arr.map(([_, v]: any) => convert(v?.medianRaw) ?? null),
          arr.map(([_, v]: any) => [convert(v?.minRaw) ?? null, convert(v?.maxRaw) ?? null]),
          name, unit
        )
      })
      setCharts(charts)
    }
  }, [rooms])

  return (
    <div>
      <div className="container">
        <div className="m-3">
          <select className="form-select"
                  aria-label="Select the room"
                  value={selectedRoom?.id}
                  onChange={onRoomChange}>
            <option>Select the room</option>
            {rooms.map(r => <option value={r.id}>{r.name}</option>)}
          </select>
        </div>
      </div>
      <div className={chartStyles.container}>
        {selectedRoom && !_.isEmpty(charts) && charts.map(option =>
          <div className={chartStyles.item}>
            <HighchartsReact
              highcharts={Highcharts}
              options={option} />
          </div>
        )}
      </div>
    </div>
  )
}
