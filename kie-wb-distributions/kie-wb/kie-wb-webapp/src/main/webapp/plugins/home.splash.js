$registerSplashScreen({
    id: 'home.splash',
    template: '<div id=\"myCarousel\" class=\"carousel slide\">    <div class=\"carousel-inner\">        <div class=\"item active\">            <img src=\"images/jbpm_lifecycle_dark.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Full project lifecycle support</h4>                <p>You can now author, test, build, deploy, and manage your projects and their content from one place.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/jbpm_customize_screen.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Multi-view working environment</h4>                <p>Increase your productivity by customizing your workspace, and easily flip between working files without re-opening.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/git_powered.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Git based repositories</h4>                <p>Repositories are now based on git, one of the most popular version controls systems available.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/process_simulate.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Simulate your processes</h4>                <p>You can now simulate your business processes with various times and variables to thoroughly test for areas of improvement.</p>            </div>        </div>    <div class=\"item\">            <img src=\"images/form_modeler.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Form Modeler</h4>                <p>You can now design forms to capture and display information during process task execution, without needing any coding or template markup skills.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/data_modeler.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Data Modeler</h4>                <p>This new authoring tool will allow you to create  business entities and link them into your processes and rules.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/dashboard_builder.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Dashboard Builder</h4>                <p>Powerful tooling for the visual composition of fully customizable business dashboards which may be feed with data coming from heterogeneous sources of information.</p>            </div>        </div>                </div>    <a class=\"left carousel-control\" href=\"#myCarousel\" data-slide=\"prev\">&lsaquo;</a>    <a class=\"right carousel-control\" href=\"#myCarousel\" data-slide=\"next\">&rsaquo;</a></div>',
    body_height: 285,
    title: 'What\'s New',
    enabled: false,
    display_next_time: false,
    interception_points: ['org.kie.workbench.common.screens.home.client.perspectives.HomePerspective']
});
