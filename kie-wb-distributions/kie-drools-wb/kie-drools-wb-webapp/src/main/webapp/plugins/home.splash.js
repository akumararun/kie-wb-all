$registerSplashScreen({
    id: 'home.splash',
    template: '<div id=\"myCarousel\" class=\"carousel slide\">    <div class=\"carousel-inner\">        <div class=\"item active\">            <img src=\"images/drools_lifecycle.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Full project lifecycle support</h4>                <p>You can now author, test, build, deploy, and manage your projects and their content from one place.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/drools_customize_screen.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Multi-view working environment</h4>                <p>Increase your productivity by customizing your workspace, and easily flip between working files without re-opening.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/git_powered.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>Git based repositories</h4>                <p>Repositories are now based on git, one of the most popular version controls systems available.</p>            </div>        </div>        <div class=\"item\">            <img src=\"images/jarmodel.png\" alt=\"\">            <div class=\"carousel-caption\">                <h4>New programming model</h4>                <p>Publish and consume rules as simple JAR files following maven conventions thanks to our new java standard packaging programming model.</p>            </div>        </div>    </div>    <a class=\"left carousel-control\" href=\"#myCarousel\" data-slide=\"prev\">‹</a>    <a class=\"right carousel-control\" href=\"#myCarousel\" data-slide=\"next\">›</a></div>',
    title: function () {
        return 'What\'s New';
    },
    display_next_time: true,
    interception_points: ['org.kie.workbench.common.screens.home.client.perspectives.HomePerspective']
});
