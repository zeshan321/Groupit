class HomeController < ApplicationController
def index
end
def test
WebsocketRails[:chat].trigger(:save,"test messages.save",:namespace => :messages)
redirect root_path
end
end
