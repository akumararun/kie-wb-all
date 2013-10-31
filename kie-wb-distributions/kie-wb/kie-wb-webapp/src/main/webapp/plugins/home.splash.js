$registerSplashScreen({
    id: 'home.splash',
    template: '<div id=\"myCarousel\" class=\"carousel slide\">    <div class=\"carousel-inner\">        <div class=\"item active\">            <img src=\"images/jbpm_lifecycle_dark.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Full project lifecycle support</h4>                <p>You can now author, test, build, deploy, and manage your projects and their content from one place.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/jbpm_customize_screen.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Multi-view working environment</h4>                <p>Increase your productivity by customizing your workspace, and easily flip between working files without re-opening.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/git_powered.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Git based repositories</h4>                <p>Repositories are now based on git, one of the most popular version controls systems available.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/process_simulate.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Simulate your processes</h4>                <p>You can now simulate your business processes with various times and variables to thoroughly test for areas of improvement.</p>            </div>        </div>    </div>    <a class=\"left carousel-control\" href=\"#myCarousel\" data-slide=\"prev\">‹</a>    <a class=\"right carousel-control\" href=\"#myCarousel\" data-slide=\"next\">›</a></div>',
    title: function () {
        return 'What\'s New';
    },
    display_next_time: true,
    interception_points: ['org.kie.workbench.common.screens.home.client.perspectives.HomePerspective']
});
