
function generateTable(data) {
    $("#schedules").empty();
    for (var i = 0; i < data.length; i++) {
        $("<div id='" + "schedule-" + (i + 1) + "'></div>").appendTo($("#schedules")).addClass("schedule");
        $("#schedule" + (i + 1)).append(generateScheduleContent(i, data[i]));
    }
}

function generateScheduleContent(scheduleIndex, data) {
    coursesInfoId = "schedule-courses-info-" + (scheduleIndex + 1);
    tableId = "schedule-courses-table-" + (scheduleIndex + 1);
    coursesInfo = data.courseInfos;
    schedule = data.table;
    $("#schedule-" + (scheduleIndex + 1)).append("<div id='" + coursesInfoId + "'></div>");
    $("#schedule-" + (scheduleIndex + 1)).append("<div id='" + tableId + "'></div>")
    info = $("#" + coursesInfoId);
    info.addClass("course-info");
    mainTable = $("#" + tableId);
    mainTable.addClass("table");

    /*fill in course info*/
    for (i = 0; i < coursesInfo.length; i++) {
        info.append(div(generateCourse(coursesInfo[i]), "course-info"));
    }

    /*fill in week table*/
    for (i = 0; i < schedule.length; i++) {
        classInstance = schedule[i];
        mainTable.append(generateClassDiv(scheduleIndex, classInstance));
        setRowNum(scheduleIndex, classInstance);
    }
}

function generateCourse(coursesInfo) {
    result = span(coursesInfo.name, "info");
    if (coursesInfo.lecNum != -1) {
        result = result.concat(span("Lec " + (coursesInfo.lecNum+1), "info"));
    } else {
        result = result.concat(span("-", "info"));
    }
    if (coursesInfo.tutNum != -1) {
        result = result.concat(span("Tut " + (coursesInfo.tutNum+1), "info"));
    } else {
        result = result.concat(span("-", "info"));
    }
    if (coursesInfo.labNum != -1) {
        result = result.concat(span("Lab " + (coursesInfo.labNum+1), "info"));
    } else {
        result = result.concat(span("-", "info"));
    }
    return result;
}

function setRowNum(scheduleIndex, classInstance) {
    time = classInstance.time;
    start = time.split("-")[0];
    end = time.split("-")[1];
    startRow = getRowNum(start);
    endRow = getRowNum(end);
    id = "#schedule-" + (scheduleIndex + 1) + "-class-" + classInstance.id;
    $(id).css({
        "grid-column": classInstance.day,
        "grid-row": startRow + "/" + endRow
    });
}

function generateClassDiv(scheduleIndex, classInstance) {
    return "<div class='grid-class' id='schedule-" + (scheduleIndex + 1) + "-class-" +
        classInstance.id + "'>" +
        span(classInstance.name.toUpperCase()) +
        span(classInstance.section) +
        span(classInstance.time) + "</div>";
}

/**
 * surrand input with <span>
 * @param {string} text 
 */
function span(text, className = "") {
    return "<span class='" + className + "'>" + text + "</span>";
}

function div(data, className = "") {
    if (className != "") {
        return "<div class='" + className + "'>" + data + "</div>";
    }
    return "<div>" + data + "</div>";
}

/**
 * get hard-coded data
 */
function getData() {
    return [
        {
            "coursesInfo": [
                {
                    "name": "sfwr 2aa4",
                    "lecNum": 1,
                    "tutNum": 1,
                    "labNum": -1
                }
            ],

            "schedule": [
                {
                    "id": 0,
                    "name": "sfwr 2aa4",
                    "day": 1,
                    "time": "09:30-10:20",
                    "section": "lab01",
                },
                {
                    "id": 1,
                    "name": "sfwr 2aa4",
                    "day": 1,
                    "time": "08:30-9:20",
                    "section": "lec01",
                },
                {
                    "id": 2,
                    "name": "sfwr 2aa4",
                    "day": 2,
                    "time": "12:30-13:20",
                    "section": "lab01",
                },
                {
                    "id": 4,
                    "name": "sfwr 2aa4",
                    "day": 2,
                    "time": "08:00-8:30",
                    "section": "lab01",
                },
                {
                    "id": 3,
                    "name": "stats 3y03",
                    "day": 5,
                    "time": "19:00-22:00",
                    "section": "lec01",
                },
                {
                    "id": 5,
                    "name": "stats 3y03",
                    "day": 2,
                    "time": "8:30-9:00",
                    "section": "lec01",
                }
            ]
        }, {
            "coursesInfo": [
                {
                    "name": "sfwr 2aa4",
                    "lecNum": 1,
                    "tutNum": 1,
                    "labNum": -1
                }
            ],

            "schedule": [
                {
                    "id": 0,
                    "name": "sfwr 2aa4",
                    "day": 1,
                    "time": "09:30-10:20",
                    "section": "lab01",
                },
                {
                    "id": 1,
                    "name": "sfwr 2aa4",
                    "day": 1,
                    "time": "08:30-9:20",
                    "section": "lec01",
                },
                {
                    "id": 2,
                    "name": "sfwr 2aa4",
                    "day": 2,
                    "time": "12:30-13:20",
                    "section": "lab01",
                },
                {
                    "id": 3,
                    "name": "stats 3y03",
                    "day": 5,
                    "time": "19:00-22:00",
                    "section": "lec01",
                }
            ]
        }
    ];
}

/**
 * given a time return the number of row.
 * e.g. "08:30" gives "2"
 * @param {string} time 
 */
function getRowNum(time) {
    hour = time.split(":")[0];
    minute = time.split(":")[1];
    result = (hour - 7)*2-1;
    if (minute != "00") {
        result = result + 1;
    }
    return result;
}
