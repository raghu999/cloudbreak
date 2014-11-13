<div id="panel-events" class="col-md-12" ng-controller="eventController">
    <div class="panel panel-default">
        <div class="panel-heading panel-heading-nav">
            <a href="" id="events-btn" class="btn btn-info btn-fa-2x" role="button" data-toggle="collapse"
               data-target="#panel-events-collapse"><i class="fa fa-angle-down fa-2x fa-fw-forced"></i></a>
            <h4>events report</h4>
        </div>

        <div id="panel-events-collapse" class="panel-btn-in-header-collapse collapse">
            <div class="panel-body">

                <h5>
                    <i class="fa fa-filter fa-fw"></i>
                    filters</h5>

                <form class="row row-filter">
                    <div class="col-xs-6 col-sm-3 col-md-2">
                        <label for="cloudProvider">cloud provider</label>

                        <div>
                            <select class="form-control input-sm" id="cloudProvider" ng-model="localFilter.cloud">
                                <option>all</option>
                                <option value="AWS">Amazon EC2</option>
                                <option value="AZURE">Microsoft Azure</option>
                                <option value="GCC">Google Cloud Compute</option>
                            </select>
                        </div>
                    </div>
                    <div class="col-xs-6 col-sm-3 col-md-4">
                        <label for="user">user</label>

                        <div>
                            <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-search"></i>
                                        </span>
                                <input class="form-control input-sm" type="text" ng-model="localFilter.user" id="user">
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-6 col-sm-3 col-md-4">
                        <label for="user">eventType</label>

                        <div>
                            <select class="form-control input-sm" id="eventType" ng-model="localFilter.eventType">
                                <option>all</option>
                                <option value="REQUESTED">REQUESTED</option>
                                <option value="CREATE_IN_PROGRESS">CREATE_IN_PROGRESS</option>
                                <option value="AVAILABLE">AVAILABLE</option>
                                <option value="UPDATE_IN_PROGRESS">UPDATE_IN_PROGRESS</option>
                                <option value="CREATE_FAILED">CREATE_FAILED</option>
                                <option value="DELETE_IN_PROGRESS">DELETE_IN_PROGRESS</option>
                                <option value="DELETE_FAILED">DELETE_FAILED</option>
                                <option value="DELETE_COMPLETED">DELETE_COMPLETED</option>
                                <option value="STOPPED">STOPPED</option>
                                <option value="STOP_REQUESTED">STOP_REQUESTED</option>
                                <option value="START_REQUESTED">START_REQUESTED</option>
                                <option value="STOP_IN_PROGRESS">STOP_IN_PROGRESS</option>
                                <option value="START_IN_PROGRESS">START_IN_PROGRESS</option>
                                <option value="START_IN_PROGRESS">START_IN_PROGRESS</option>
                                <option value="START_FAILED">START_FAILED</option>
                                <option value="STOP_FAILED">STOP_FAILED</option>
                                <option value="BILLING_STARTED">BILLING_STARTED</option>
                                <option value="BILLING_STOPPED">BILLING_STOPPED</option>
                            </select>
                        </div>
                    </div>



                    <div class="col-xs-6 col-sm-3 col-md-2">
                        <a id="btnGenReport" ng-click="loadEvents()" class="btn btn-success btn-block" role="button">
                            <i class="fa fa-fw fa-refresh"></i>
                            <!-- <i class="fa fa-circle-o-notch fa-spin fa-fw"></i> --> </a>
                    </div>

                </form>
                <!-- .row -->

                <div class="table-responsive" ng-show="(events.length != 0) && events">
                    <table class="table table-report table-sortable-cols table-with-pagination ">
                        <thead>
                        <tr>
                            <th>cloud</th>
                            <th>user</th>
                            <th>type</th>
                            <th>timestamp</th>
                            <th style="width: 25%;">message</th>
                        </tr>
                        </thead>
                        <tbody>
                            <tr ng-repeat="event in events| filter:eventFilterFunction">
                                <td>{{event.cloud}}</td>
                                <td>{{event.owner}}</td>
                                <td>{{event.eventType}}</td>
                                <td>{{event.eventTimestamp}}</td>
                                <td style="width: 25%;">{{event.eventMessage}}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

            </div>

        </div>
    </div>
</div>