package rip.deadcode.sandbox_pi.http.handler.ui_classic

import cats.effect.IO
import com.google.common.net.MediaType
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.StringHttpResponse
import rip.deadcode.sandbox_pi.http.handler.ui_classic.UiClassicHandler.Body
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

// temporal ui impl.
// gonna create spa as a separate project

@Singleton
class UiClassicHandler @Inject() () extends HttpHandler {

  override def url: Regex = "^/ui-classic$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    IO {
      StringHttpResponse(
        200,
        MediaType.HTML_UTF_8,
        Body
      )
    }
  }
}

private object UiClassicHandler {
  val Body =
    // language=HTML
    """<!DOCTYPE html>
      |<html lang="en">
      |<head>
      |  <meta charset="utf-8">
      |  <title>sandbox-pi</title>
      |  <meta name="viewport" content="width=device-width, initial-scale=1">
      |  <script src="https://code.highcharts.com/highcharts.js"></script>
      |  <script src="http://code.highcharts.com/highcharts-more.js"></script>
      |  <style>
      |    .container {
      |      width: 100dvw;
      |      height: 100dvh;
      |      display: grid;
      |      grid-template-columns: 1fr 1fr;
      |    }
      |    .item {
      |      width: 50dvw;
      |      height: 50dvh;
      |    }
      |    @media only screen and (max-width: 768px) {
      |      .container {
      |        grid-template-columns: 1fr;
      |      }
      |      .item {
      |        width: 100dvw;
      |      }
      |    }
      |  </style>
      |</head>
      |<body style="margin: 0">
      |  <div class="container">
      |    <div id="c_t" class="item" "></div>
      |    <div id="c_p" class="item""></div>
      |    <div id="c_h" class="item""></div>
      |    <div id="c_c" class="item""></div>
      |  </div>
      |  <script>
      |    document.addEventListener("DOMContentLoaded", () => {
      |      fetch("/stat?room_id=57c91c78-99db-49fe-ae88-f9ef723aca9b&p=day").then(response => {
      |        if (response.ok) {
      |          return response.json()
      |        } else {
      |          throw new Error(`Response: ${response.status} ${response.statusText}}`)
      |        }
      |      }).then(data => {
      |        console.log(data)
      |        const temp = Object
      |          .entries(data.temperature)
      |          .sort(([x, _1], [y, _2]) => x - y)
      |        const tempMed = temp.map(([_, v]) => v?.medianRaw ?? null)
      |        const tempMinMax = temp.map(([_, v]) => [v?.minRaw ?? null, v?.maxRaw ?? null])
      |        Highcharts.chart("c_t", {
      |          title: {
      |            text: "Temperature"
      |          },
      |          xAxis: {
      |          },
      |          yAxis: {
      |            title: {
      |              text: "℃"
      |            }
      |          },
      |          series: [
      |            {
      |              name: "Temperature",
      |              data: tempMed,
      |              marker: {
      |              },
      |              connectNulls: true
      |            },
      |            {
      |              name: "Max-Min",
      |              type: "arearange",
      |              data: tempMinMax,
      |              lineWidth: 0,
      |              color: Highcharts.getOptions().colors[0],
      |              fillOpacity: 0.3,
      |              marker: { enabled: false },
      |              connectNulls: true
      |            }
      |          ]
      |        })
      |        const p = Object
      |          .entries(data.pressure)
      |          .sort(([x, _1], [y, _2]) => x - y)
      |        const pMed = p.map(([_, v]) => v?.medianRaw ?? null)
      |        const pMinMax = p.map(([_, v]) => [v?.minRaw ?? null, v?.maxRaw ?? null])
      |        Highcharts.chart("c_p", {
      |          title: {
      |            text: "Pressure"
      |          },
      |          xAxis: {
      |          },
      |          yAxis: {
      |            title: {
      |              text: "㎩"
      |            }
      |          },
      |          series: [
      |            {
      |              name: "Pressure",
      |              data: pMed,
      |              marker: {
      |              },
      |              connectNulls: true
      |            },
      |            {
      |              name: "Max-Min",
      |              type: "arearange",
      |              data: pMinMax,
      |              lineWidth: 0,
      |              color: Highcharts.getOptions().colors[0],
      |              fillOpacity: 0.3,
      |              marker: { enabled: false },
      |              connectNulls: true
      |            }
      |          ]
      |        })
      |        const h = Object
      |          .entries(data.humidity)
      |          .sort(([x, _1], [y, _2]) => x - y)
      |        const hMed = h.map(([_, v]) => v?.medianRaw ?? null)
      |        const hMinMax = h.map(([_, v]) => [v?.minRaw ?? null, v?.maxRaw ?? null])
      |        Highcharts.chart("c_h", {
      |          title: {
      |            text: "Humidity"
      |          },
      |          xAxis: {
      |          },
      |          yAxis: {
      |            title: {
      |              text: "％"
      |            }
      |          },
      |          series: [
      |            {
      |              name: "Humidity",
      |              data: hMed,
      |              marker: {
      |              },
      |              connectNulls: true
      |            },
      |            {
      |              name: "Max-Min",
      |              type: "arearange",
      |              data: hMinMax,
      |              lineWidth: 0,
      |              color: Highcharts.getOptions().colors[0],
      |              fillOpacity: 0.3,
      |              marker: { enabled: false },
      |              connectNulls: true
      |            }
      |          ]
      |        })
      |        const c = Object
      |          .entries(data.co2)
      |          .sort(([x, _1], [y, _2]) => x - y)
      |        const cMed = c.map(([_, v]) => v?.medianRaw ?? null)
      |        const cMinMax = c.map(([_, v]) => [v?.minRaw ?? null, v?.maxRaw ?? null])
      |        Highcharts.chart("c_c", {
      |          title: {
      |            text: "CO2"
      |          },
      |          xAxis: {
      |          },
      |          yAxis: {
      |            title: {
      |              text: "㏙"
      |            }
      |          },
      |          series: [
      |            {
      |              name: "CO2",
      |              data: cMed,
      |              marker: {
      |              },
      |              connectNulls: true
      |            },
      |            {
      |              name: "Max-Min",
      |              type: "arearange",
      |              data: cMinMax,
      |              lineWidth: 0,
      |              color: Highcharts.getOptions().colors[0],
      |              fillOpacity: 0.3,
      |              marker: { enabled: false },
      |              connectNulls: true
      |            }
      |          ]
      |        })
      |      })
      |    })
      |  </script>
      |</body>
      |</html>
      |""".stripMargin
}
